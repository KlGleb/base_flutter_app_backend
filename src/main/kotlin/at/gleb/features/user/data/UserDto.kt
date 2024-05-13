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
    val resetPasswordCode: OneTimeCodeDto? = null
)

data class OneTimeCodeDto(
    val created: Date = Date(),
    val token: String,
    val key: String,
)