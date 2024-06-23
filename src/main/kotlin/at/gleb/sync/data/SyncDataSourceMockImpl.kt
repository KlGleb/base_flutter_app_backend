package at.gleb.sync.data

import java.util.*

class SyncDataSourceMockImpl : SyncDataSource {
    override suspend fun getData(): List<CounterpartyDto> {
        return listOf(
            CounterpartyDto(
                code = "code1",
                inn = "inn1",
                email = "info@gleb.at",
                name = "Общество с ограниченной ответственностью «МИР»",
                bills = listOf(
                    BillDto(
                        id = "bill1",
                        name = "Общество с ограниченной ответственностью «МИР»",
                        account = "40602810206000050025",
                        bankName = "Уральский банк ПАО Сбербанк г. Екатеринбург",
                        corBill = "30101810500000000674",
                        amount = 10000,
                        created = Date().toString(),
                        paid = false,
                        bik = "046577674",
                        qrData = "some text qr data",
                    ),
                    BillDto(
                        id = "bill2",
                        name = "bill2 name",
                        account = "40602810206000050025",
                        bankName = "Уральский банк ПАО Сбербанк г. Екатеринбург",
                        corBill = "30101810500000000674",
                        bik = "046577674",
                        amount = 10000,
                        created = Date().toString(),
                        paid = false,
                        qrData = "some text qr data",
                    ),
                ),
                devices = listOf(
                    DeviceDto(
                        id = "device1",
                        phoneNumber = "+79680464545",
                        machineName = "Кофемашина 1"
                    ),
                    DeviceDto(
                        id = "device2",
                        phoneNumber = "+79680464546",
                        machineName = "Кофемашина 2"
                    ),
                ),
            )
        )
    }
}

