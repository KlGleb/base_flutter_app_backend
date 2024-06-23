package at.gleb.features.user.data

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId
import java.util.*

data class UserDto(
    @BsonProperty("_id")
    @BsonId
    val id: ObjectId? = null,
    val email: String,
    val password: String,
    val resetPasswordCode: OneTimeCodeDto? = null,
    val confirmEmailCode: OneTimeCodeDto? = null,
    val messagingTokens: MutableMap<String, MsgToken>? = mutableMapOf(),
    val settings: UserSettings? = UserSettings(),
    val emailConfirmed: Boolean? = false
)

data class OneTimeCodeDto(
    val created: Date = Date(),
    val token: String,
    val key: String,
)

data class MsgToken(
    val token: String,
    val os: String,
    val osVersion: String,
    var enabled: Boolean
)

data class UserSettings(
    val notificationsEmail: Boolean = true
)