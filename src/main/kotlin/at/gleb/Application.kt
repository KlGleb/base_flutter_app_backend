package at.gleb

import at.gleb.cupcloud.appModule
import at.gleb.cupcloud.auth.configureAuth
import at.gleb.cupcloud.exceptions.configureExceptions
import at.gleb.plugins.configureHTTP
import at.gleb.plugins.configureSerialization
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
    configureExceptions()
}
