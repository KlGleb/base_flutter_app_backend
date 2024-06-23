package at.gleb.notifications

import at.gleb.features.user.data.UserDto

class NotificatorSchedulerImpl : NotificatorScheduler {

    override fun sendNotification(user: UserDto, message: String) {
        Thread.sleep(3000)
        println("Send notification to $user: $message")
    }
}