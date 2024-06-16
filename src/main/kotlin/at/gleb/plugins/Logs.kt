package at.gleb.plugins

import io.ktor.content.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import io.ktor.util.logging.*
import kotlinx.coroutines.runBlocking

fun Application.configureLogs() {
    install(CallId) {
        generate(32)
        header(HttpHeaders.XRequestId)
    }
    install(LogAttributesPlugin)

    install(CallLogging) {
        callIdMdc("call_id")
        mdc("user_id") { it.logAttributes?.userId }
        mdc("remote_host") { it.logAttributes?.remoteHost }
        mdc("remote_address") { it.logAttributes?.remoteAddress }
        mdc("local_remote_address") { it.logAttributes?.localRemoteAddress }
        mdc("local_remote_host") { it.logAttributes?.localRemoteHost }
    }

    val logger = KtorSimpleLogger(name = "at.gleb.plugins.Application.configureCallId")
    val plugin = createApplicationPlugin("logPlugin") {
        onCall { call ->
            val k = runBlocking {
                call.receiveText()
            }
            logger.debug("request body: $k")
        }

        onCallRespond { call, body ->
            if (body is TextContent) {
                logger.debug("Response body for call ${call.request.uri}: \n ${body.text}")
            } else {
                logger.debug("Response body for call ${call.request.uri}: \n $body")
            }
        }
    }

    install(plugin)
}
