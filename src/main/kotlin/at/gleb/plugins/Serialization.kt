package at.gleb.plugins

import com.fasterxml.jackson.databind.*
import io.ktor.serialization.gson.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        gson()
        /*jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }*/
    }
    routing {
        get("/") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}
