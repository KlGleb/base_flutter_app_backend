package at.gleb.reviewmagic.exceptions

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureExceptions() {
    install(StatusPages) {
        exception<Throwable> { call, e ->
            call.application.log.error("Unsuccessful response", e)
            when {
                e.cause is MyException -> {
                    val myProjectException = e.cause!! as MyException
                    call.respond(myProjectException.httpStatusCode, myProjectException.toVo())
                }

                e is MyException -> {
                    call.respond(e.httpStatusCode, e.toVo())
                }

                else -> {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        UnknownException().toVo()
                    )
                }
            }
        }
    }
}

