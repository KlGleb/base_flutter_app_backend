package at.gleb

import at.gleb.auth.configureAuth
import at.gleb.features.auth.cupcloud.appModule
import at.gleb.features.auth.cupcloud.exceptions.configureExceptions
import at.gleb.graphql.configureGraphQl
import at.gleb.notifications.startNotificationWorker
import at.gleb.plugins.configureHTTP
import at.gleb.plugins.configureLogs
import at.gleb.plugins.configureSerialization
import at.gleb.sync.presentation.startSyncWorker
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    install(Koin) {
        modules(appModule())
    }
    configureSerialization()
    configureHTTP()
    configureAuth()
    configureExceptions()
    configureGraphQl()
    configureLogs()

    startSyncWorker()
    startNotificationWorker()
}
