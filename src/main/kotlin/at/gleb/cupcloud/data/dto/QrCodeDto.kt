package at.gleb.cupcloud.data.dto

data class QrCodeDto(
    val nane: String = "",
    val companyId: String,
    val archived: Boolean = false,
)
