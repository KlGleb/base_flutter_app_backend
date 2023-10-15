package at.gleb

import at.gleb.plugins.*
import at.gleb.reviewmagic.appModule
import at.gleb.reviewmagic.auth.configureAuth
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.koin.ktor.plugin.Koin

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(Koin) {
        modules(appModule())
    }
    configureSerialization()
    configureHTTP()
    configureAuth()
    configureRouting()


}
