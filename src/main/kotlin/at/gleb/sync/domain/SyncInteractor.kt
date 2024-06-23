package at.gleb.sync.domain

import at.gleb.counterparty.data.BillDbDto
import at.gleb.counterparty.data.CounterpartyDbDataSource
import at.gleb.counterparty.data.CounterpartyDbDto
import at.gleb.counterparty.data.DeviceDbDto
import at.gleb.features.user.data.UserDataSource
import at.gleb.features.user.data.UserDto
import at.gleb.notifications.NotificatorScheduler
import at.gleb.sync.data.SyncDataSource
import io.ktor.util.logging.*


class SyncInteractor(
    private val syncDataSource: SyncDataSource,
    private val dbDataSource: CounterpartyDbDataSource,
    private val notificatorScheduler: NotificatorScheduler,
    private val userDataSource: UserDataSource,
) {
    private val logger = KtorSimpleLogger(this::class.java.name)
    suspend fun sync() {
        logger.debug("Starting sync data")
        val newData = syncDataSource.getData()
        val oldData = dbDataSource.getAll()
        logger.debug("All the data received, new data size is ${newData.size}")

        val dataForUpdate = mutableListOf<CounterpartyDbDto>()
        val userMap = mutableMapOf<String, UserDto?>()

        newData.forEach { newDto ->
            val oldDto = oldData.find {
                it.email == newDto.email && it.inn == newDto.inn
            }
            val newBills = mutableListOf<BillDbDto>()
            val newDevices = mutableListOf<DeviceDbDto>()

            newDto.bills?.forEach { bill ->
                val ob = oldDto?.bills?.find { oldBill ->
                    oldBill.id == bill.id
                }

                if (ob == null) {
                    logger.debug("New bill detected: $bill")
                    if (!userMap.containsKey(newDto.email)) {
                        val byEmail = userDataSource.getByEmail(newDto.email)
                        userMap[newDto.email] = byEmail
                    }
                    userMap[newDto.email]?.let {
                        notificatorScheduler.sendNotification(it, "У вас новый счет на ${bill.amount} ₽")
                    }
                }

                newBills.add(
                    BillDbDto(
                        _id = ob?._id,
                        id = bill.id,
                        name = bill.name,
                        account = bill.account,
                        bankName = bill.bankName,
                        corBill = bill.corBill,
                        amount = bill.amount,
                        created = bill.created,
                        paid = bill.paid,
                        bik = bill.bik,
                    )
                )
            }
            newDto.devices?.forEach {
                val od = oldDto?.devices?.find { oldDevice ->
                    oldDevice.id == it.id
                }

                newDevices.add(
                    DeviceDbDto(
                        _id = od?._id,
                        id = it.id,
                        phoneNumber = it.phoneNumber,
                        machineName = it.machineName,
                    )
                )
            }

            //todo: выключить устройство

            val dtoForUpdate = CounterpartyDbDto(
                _id = oldDto?._id,
                code = newDto.code,
                inn = newDto.inn,
                email = newDto.email,
                name = newDto.name,
                bills = newBills,
                devices = newDevices
            )

            dataForUpdate.add(dtoForUpdate)
        }

        logger.debug("Start replacing data")
        dbDataSource.replaceAll(dataForUpdate)
        logger.debug("Sync data successfully finished")
    }
}