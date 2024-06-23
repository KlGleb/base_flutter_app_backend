package at.gleb.counterparty.data

import at.gleb.features.auth.cupcloud.Cols
import com.mongodb.client.model.Filters
import kotlinx.coroutines.flow.toList
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

class CounterpartyDbDataSource(private val cols: Cols) {
    suspend fun getAll(): List<CounterpartyDbDto> = cols.counterparties.find().toList()

    /*suspend fun getByEmail(email: String): List<CounterpartyDbDto> =
        cols.counterparties.find(Filters.eq(CounterpartyDbDto::email.name, email)).toList()
*/
    suspend fun replaceAll(data: List<CounterpartyDbDto>) {
        cols.counterparties.deleteMany(Filters.empty())
        cols.counterparties.insertMany(data)
    }
}


data class CounterpartyDbDto(
    @BsonProperty("_id")
    @BsonId
    val _id: ObjectId? = null,
    val code: String?,
    val inn: String,
    val email: String,
    val name: String,
    val bills: List<BillDbDto>?,
    val devices: List<DeviceDbDto>?

)

data class BillDbDto(
    @BsonProperty("_id")
    @BsonId
    val _id: ObjectId? = null,
    val id: String,
    val name: String,
    val account: String,
    val bankName: String,
    val corBill: String,
    val amount: Long,
    val created: String,
    val paid: Boolean,
    val bik: String,
)

data class DeviceDbDto(
    @BsonProperty("_id")
    @BsonId
    val _id: ObjectId? = null,
    val id: String?,
    val phoneNumber: String,
    val machineName: String,
)