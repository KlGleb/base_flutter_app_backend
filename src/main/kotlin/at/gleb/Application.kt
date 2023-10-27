package at.gleb

import at.gleb.plugins.configureHTTP
import at.gleb.plugins.configureRouting
import at.gleb.plugins.configureSerialization
import at.gleb.reviewmagic.appModule
import at.gleb.reviewmagic.auth.configureAuth
import at.gleb.reviewmagic.exceptions.configureExceptions
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    install(Koin) {
        modules(appModule())
    }
    configureSerialization()
    configureHTTP()
    configureAuth()
    configureRouting()
    configureExceptions()
}
