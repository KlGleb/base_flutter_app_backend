package at.gleb.cupcloud.data.dto

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

data class UserDto(
    @BsonProperty("_id")
    @BsonId
    val id: ObjectId? = null,
    val email: String,
    val password: String,
)
