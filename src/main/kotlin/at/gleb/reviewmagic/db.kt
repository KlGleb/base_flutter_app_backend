package at.gleb.reviewmagic

import at.gleb.reviewmagic.data.dto.UserDto
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.getCollection

class Cols(private val db: MongoDatabase) {
    val users by lazy {
        db.getCollection<UserDto>().apply {
            ensureUniqueIndex(UserDto::email)
        }
    }
}
