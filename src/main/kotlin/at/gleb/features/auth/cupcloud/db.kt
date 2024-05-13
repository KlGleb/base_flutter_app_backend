package at.gleb.features.auth.cupcloud

import at.gleb.features.user.data.UserDto
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KProperty


class Cols(private val db: MongoDatabase) {
    val users by lazy { db.getCollection<UserDto>("users") }

    init {
        runBlocking {
            ensureIndexes()
        }
    }

    private suspend fun ensureIndexes() {
        users.createUniqueIndex(UserDto::email)
    }
}

suspend fun <T : Any> MongoCollection<T>.createUniqueIndex(property: KProperty<String>) {
    createIndex(Indexes.ascending(property.name), IndexOptions().unique(true))
}
