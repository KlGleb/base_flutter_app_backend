package at.gleb.notifications

import at.gleb.features.user.data.MsgToken
import com.google.firebase.messaging.BatchResponse
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.MulticastMessage
import io.ktor.util.logging.*

typealias FbNotification = com.google.firebase.messaging.Notification

class PushNotificator {
    private val logger = KtorSimpleLogger(this::class.java.name)

    fun sendPushNotification(allTokens: Map<String, MsgToken>?, message: String, link: String): BatchResponse? {
        try {
            val tokens = allTokens?.values?.filter { it.enabled }?.map { it.token }
            if (tokens.isNullOrEmpty()) return null

            val msg = MulticastMessage.builder().apply {
                putData("link", link)
                setNotification(
                    FbNotification.builder().apply {
                        setBody(message)
                    }.build()
                )
                addAllTokens(tokens)
            }.build()

            return FirebaseMessaging.getInstance().sendEachForMulticast(msg)
        } catch (e: Exception) {
            logger.error("Cannot sent push notification with message=[$message], link=[$link]", e)
            return null
        }
    }
}