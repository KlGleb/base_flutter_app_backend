package at.gleb.notifications

import at.gleb.features.user.data.UserDto

interface NotificatorScheduler {
    fun sendConfirmEmail(email: String, codeReason: String, code: String) {
        val data = NotificationData(
            mail = MailNotificationData(
                email = email,
                htmlMessage = "<p>Ваш код подтверждения $codeReason:<br/><br/><center><b>$code</b></center></p>"
            )
        )

        notificationsQueue.add(data)
    }

    fun sendNotification(user: UserDto, message: String)
}

