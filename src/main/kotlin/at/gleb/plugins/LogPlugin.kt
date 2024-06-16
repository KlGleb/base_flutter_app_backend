package at.gleb.plugins

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import io.ktor.server.plugins.*
import io.ktor.util.*
import io.ktor.util.logging.*
import org.koin.java.KoinJavaComponent
import java.util.*

internal val logAttributesKey: AttributeKey<LogAttributes> = AttributeKey("LogAttributes")
private val gson: Gson by KoinJavaComponent.inject(Gson::class.java)

val logger = KtorSimpleLogger("LogAttributesPlugin")
val LogAttributesPlugin: RouteScopedPlugin<Unit> = createRouteScopedPlugin(
    "LogAttributes",
) {


    /*onCallReceive { call ->
        logger.info(call.request.headers.toMap().toString())
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.payload?.getClaim("id")?.asString()
        val attributes = LogAttributes(
            userId = userId,
            remoteHost = call.request.origin.remoteHost,
            remoteAddress = call.request.origin.remoteAddress,
            localRemoteAddress = call.request.local.remoteAddress,
            localRemoteHost = call.request.local.remoteHost
        )
        call.attributes.put(logAttributesKey, attributes)
    }
*/

    on(CallSetup) { call ->
        //get userId and email from token payload
        var userId: String? = null
        var email: String? = null

        val token = call.request.headers["Authorization"]?.apply {
            val arr = split(" ")
            if (arr.size >= 2) {
                this.split(" ")[1]
            }
        }

        if (token != null) {
            try {
                val payload = decodePayload(token)
                val jsonObject = gson.fromJson(payload, JsonObject::class.java)
                userId = jsonObject.get("id").asString
                email = jsonObject.get("email").asString

            } catch (e: Exception) {
                logger.error(e)
            }
        }


        val attributes = LogAttributes(
            userId = userId,
            email = email,
            remoteHost = call.request.origin.remoteHost,
            remoteAddress = call.request.origin.remoteAddress,
            localRemoteAddress = call.request.local.remoteAddress,
            localRemoteHost = call.request.local.remoteHost
        )

        call.attributes.put(logAttributesKey, attributes)
    }
}

val ApplicationCall.logAttributes: LogAttributes? get() = attributes.getOrNull(logAttributesKey)

data class LogAttributes(
    val userId: String?,
    val email: String?,
    val remoteHost: String?,
    val remoteAddress: String?,
    val localRemoteAddress: String?,
    val localRemoteHost: String?,
)


fun decodePayload(token: String): String? {
    // Разделяем токен на его компоненты
    val parts = token.split(".")
    return if (parts.size == 3) {
        // Декодируем полезную нагрузку из Base64URL
        val payload = parts[1]
        val decodedBytes = Base64.getUrlDecoder().decode(payload)
        String(decodedBytes)
    } else {
        null
    }
}