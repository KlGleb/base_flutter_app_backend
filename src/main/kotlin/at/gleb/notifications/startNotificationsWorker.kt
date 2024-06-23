package at.gleb.notifications

import at.gleb.features.user.data.MsgToken
import io.ktor.util.logging.*
import kotlinx.coroutines.*
import org.koin.java.KoinJavaComponent
import java.util.*

private val logger = KtorSimpleLogger("startNotificationWorker")

val notificationsQueue = LinkedList<NotificationData>()

private val mailNotificator: MailNotificator by KoinJavaComponent.inject(MailNotificator::class.java)
private val pushNotificator: PushNotificator by KoinJavaComponent.inject(PushNotificator::class.java)

@OptIn(DelicateCoroutinesApi::class)
fun startNotificationWorker() {
    GlobalScope.launch(Dispatchers.IO) {
        while (isActive) {
            if (notificationsQueue.isEmpty()) {
                delay(2000)
            } else {
                withTimeout(10000) {
                    val notification = notificationsQueue.poll()
                    logger.debug("Send notification $notification")

                    notification.mail?.let { mailData ->
                        logger.debug("Sending email $mailData")
                        try {
                            mailNotificator.sendEmail(
                                emailAddress = mailData.email,
                                userName = mailData.userName.orEmpty(),
                                preheader = mailData.preheader.orEmpty(),
                                message = mailData.htmlMessage
                                    ?: mailData.message
                                    ?: throw Exception("html message or message must be provided"),
                                disableNotificationsCode = mailData.disableNotificationsCode,
                            )
                        } catch (e: Exception) {
                            logger.error("Cannot send email $mailData", e)
                        }
                    }


                    notification.push?.let { push ->
                        try {
                            pushNotificator.sendPushNotification(
                                allTokens = push.tokens,
                                message = push.message,
                                link = push.link
                            )
                        } catch (e: Exception) {
                            logger.error("Cannot send push $push", e)
                        }
                    }
                }
            }
        }
    }
}

data class NotificationData(
    val mail: MailNotificationData? = null,
    val push: PushNotificationData? = null,
)

data class MailNotificationData(
    val email: String,
    val htmlMessage: String? = null,
    val message: String? = null,
    val userName: String? = null,
    val preheader: String? = null,
    val disableNotificationsCode: String? = null,

    )

data class PushNotificationData(
    val tokens: Map<String, MsgToken>,
    val link: String,
    val message: String,
)

