package at.gleb.sync.data

interface SyncDataSource {
    suspend fun getData(): List<CounterpartyDto>
}


data class CounterpartyDto(
    val code: String?,
    val inn: String,
    val email: String,
    val name: String,
    val bills: List<BillDto>?,
    val devices: List<DeviceDto>?
)

data class BillDto(
    val id: String,
    val name: String,
    val account: String,
    val bankName: String,
    val corBill: String,
    val amount: Long,
    val created: String,
    val paid: Boolean,
    val bik: String,
    val qrData: String,
)

data class DeviceDto(
    val id: String,
    val phoneNumber: String,
    val machineName: String,
)