package at.gleb.reviewmagic

import at.gleb.reviewmagic.data.dto.UserDto
import com.mongodb.client.MongoDatabase
import org.koin.java.KoinJavaComponent.inject
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.getCollection

object Cols {
    private val db: MongoDatabase by inject(MongoDatabase::class.java)

    val users by lazy {
        db.getCollection<UserDto>().apply {
            ensureUniqueIndex(UserDto::email)
        }
    }
}
