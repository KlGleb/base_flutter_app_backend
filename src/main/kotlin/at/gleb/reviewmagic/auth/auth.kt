package at.gleb.reviewmagic.auth

import at.gleb.reviewmagic.Cols
import at.gleb.reviewmagic.data.dto.UserDto
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.java.KoinJavaComponent.inject
import org.litote.kmongo.eq
import org.litote.kmongo.findOne

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
                    call.respond(call.user)
                }

                delete("/") {
                    interactor.deleteUser(call.user)
                    call.respond(true)
                }
            }
        }
    }
}

val ApplicationCall.user: UserDto
    get() {
        val principal = principal<JWTPrincipal>()
        val userId = principal!!.payload.getClaim("id").asString()
        return cols.users.findOne(UserDto::_id eq userId)!!
    }
