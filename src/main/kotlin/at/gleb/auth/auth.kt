package at.gleb.auth

import at.gleb.features.auth.cupcloud.Cols
import at.gleb.features.user.data.UserDto
import com.apurebase.kgraphql.schema.dsl.SchemaBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.mongodb.client.model.Filters
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import kotlinx.coroutines.flow.first
import org.bson.types.ObjectId
import org.koin.java.KoinJavaComponent.inject

const val AUTH_JWT = "auth-jwt"

private val cols: Cols by inject(Cols::class.java)
private val interactor: AuthInteractor by inject(AuthInteractor::class.java)
fun Application.configureAuth() {

    val secret = environment.config.property("ktor.jwt.secret").getString()
    val issuer = environment.config.property("ktor.jwt.issuer").getString()
    val audience = environment.config.property("ktor.jwt.audience").getString()
    val myRealm = environment.config.property("ktor.jwt.realm").getString()

    install(Authentication) {
        jwt(AUTH_JWT) {
            realm = myRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("id").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
}

fun SchemaBuilder.confAuth(environment: ApplicationEnvironment) {
    val secret = environment.config.property("ktor.jwt.secret").getString()
    val issuer = environment.config.property("ktor.jwt.issuer").getString()
    val audience = environment.config.property("ktor.jwt.audience").getString()

    query("auth") {
        resolver { email: String, password: String ->
            interactor.provideToken(email, password, audience, issuer, secret)
        }
    }

    query("signUp") {
        resolver { email: String, password: String ->
            interactor.register(email, password, audience, issuer, secret)
        }
    }

    query("sendResetPasswordCode") {
        resolver { email: String ->
            interactor.sendResetPasswordCode(email, audience, issuer, secret)
            true
        }
    }

    mutation("auth") {
        resolver { email: String, newPassword: String, code: String ->
            interactor.resetPassword(email, newPassword, code, audience, issuer, secret)
        }
    }
}


suspend fun ApplicationCall.user(): UserDto {
    val principal = principal<JWTPrincipal>()
    val userId = principal!!.payload.getClaim("id").asString()
    return cols.users.find(Filters.eq("_id", ObjectId(userId))).first()
}
