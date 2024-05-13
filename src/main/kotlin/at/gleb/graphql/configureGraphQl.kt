package at.gleb.graphql

import at.gleb.auth.AUTH_JWT
import at.gleb.auth.confAuth
import at.gleb.features.auth.cupcloud.exceptions.Unauthorised
import at.gleb.features.user.domain.UserRepository
import at.gleb.features.user.domain.vo.UserVo
import com.apurebase.kgraphql.Context
import com.apurebase.kgraphql.GraphQL
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.koin.java.KoinJavaComponent

private val repository: UserRepository by KoinJavaComponent.inject(UserRepository::class.java)

fun Application.configureGraphQl() {
    install(GraphQL) {
        schema {
            // prints the graphql response in a pretty json format
            configure {
                useDefaultPrettyPrinter = true
                playground = true
                wrapErrors = false
                timeout = 10000
                useCachingDocumentParser = false
            }


            wrap {
                authenticate(AUTH_JWT, optional = true, build = it)
            }

            context { call ->
                call.userId?.let { +AuthData(it) }
            }

            query("users") {
                resolver { ->
                    repository.getAllUsers()
                }
            }

            query("currentUser") {
                resolver { ctx: Context ->
                    repository.getUserById(ctx.authorize().userId)
                }
            }
            this.confAuth(environment)
        }
    }
}

fun Context.authorize(): AuthData = get() ?: throw Unauthorised()

suspend fun ApplicationCall.user(): UserVo {
    val principal = principal<JWTPrincipal>()
    val userId = principal!!.payload.getClaim("id").asString()
    return repository.getUserById(userId)
}

val ApplicationCall.userId: String?
    get() {
        val principal = principal<JWTPrincipal>()
        return principal?.payload?.getClaim("id")?.asString()
    }


data class AuthData(val userId: String)