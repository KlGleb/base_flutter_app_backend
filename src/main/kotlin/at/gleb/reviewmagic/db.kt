package at.gleb.reviewmagic

import at.gleb.reviewmagic.data.dto.CompanyDto
import at.gleb.reviewmagic.data.dto.QrCodeDto
import at.gleb.reviewmagic.data.dto.UserDto
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KProperty


class Cols(private val db: MongoDatabase) {
    val users by lazy { db.getCollection<UserDto>("users") }
    val companies by lazy { db.getCollection<CompanyDto>("companies") }
    val qrs by lazy { db.getCollection<QrCodeDto>("companies") }

    init {
        runBlocking {
            ensureIndexes()
        }
    }

    private suspend fun ensureIndexes() {
        users.createUniqueIndex(UserDto::email)
        companies.createIndex(Indexes.ascending(CompanyDto::userId.name))
        qrs.createIndex(Indexes.ascending(QrCodeDto::companyId.name))
    }
}

suspend fun <T : Any> MongoCollection<T>.createUniqueIndex(property: KProperty<String>) {
    createIndex(Indexes.ascending(property.name), IndexOptions().unique(true))
}
