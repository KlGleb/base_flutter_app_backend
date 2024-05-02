package at.gleb.plugins

import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        gson()
    }
    routing {
        get("/") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}
