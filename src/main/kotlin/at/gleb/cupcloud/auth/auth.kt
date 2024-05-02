package at.gleb.cupcloud.auth

import at.gleb.cupcloud.Cols
import at.gleb.cupcloud.data.dto.UserDto
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.mongodb.client.model.Filters
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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

    routing {
        route("/auth") {
            post<LoginInput>("/login") {
                call.respond(hashMapOf("token" to interactor.provideToken(it, audience, issuer, secret)))
            }

            post<RegisterInput>("/register") {
                call.respond(hashMapOf("token" to interactor.register(it, audience, issuer, secret)))
            }

            //send code to email
            post<String>("/reset") {
                interactor.sendResetPasswordCode(it, audience, issuer, secret)
                call.respond(true)
            }

            //change password
            put<ResetPasswordInput>("/reset") {
                call.respond(hashMapOf("token" to interactor.resetPassword(it, audience, issuer, secret)))
            }

            authenticate(AUTH_JWT) {
                get("/test") {
                    call.respond(call.user())
                }
            }
        }
    }
}

suspend fun ApplicationCall.user():UserDto {
    val principal = principal<JWTPrincipal>()
    val userId = principal!!.payload.getClaim("id").asString()
    return cols.users.find(Filters.eq("_id", ObjectId(userId))).first()
}
