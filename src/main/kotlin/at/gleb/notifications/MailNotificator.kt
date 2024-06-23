package at.gleb.notifications

import at.gleb.features.auth.cupcloud.utils.getRandomString
import at.gleb.notifications.utils.wrapEmail
import io.ktor.util.*
import io.ktor.util.logging.*
import org.simplejavamail.api.mailer.Mailer
import org.simplejavamail.email.EmailBuilder

class MailNotificator(private val mailer: Mailer, private val smtpFrom: String) {
    private val logger = KtorSimpleLogger(this::class.java.name)

    fun sendEmail(
        emailAddress: String,
        userName: String,
        preheader: String,
        message: String,
        disableNotificationsCode: String?,
    ) {
        logger.debug("Send email to $emailAddress, message: $message")

        if (emailAddress.endsWith("@test.at")) return

        val text = """Здравствуйте, ${userName.escapeHTML()}!
        |
        |$message
        |
        |С уважением,
        |Команда CupCloud
    """.trimMargin().wrapEmail(disableNotificationsCode, preheader)

        val email = EmailBuilder.startingBlank().apply {
            from("Команда CupCloud", smtpFrom)
            to(emailAddress)
            withSubject("CupCloud  — оповещение")
            withHTMLText(text)
            withHeader("References", getRandomString(16))
        }.buildEmail()

        mailer.sendMail(email)

        logger.debug("Email sent successfully")
    }
}
