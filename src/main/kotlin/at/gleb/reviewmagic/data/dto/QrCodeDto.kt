package at.gleb.reviewmagic.data.dto

data class QrCodeDto(
    val nane: String = "",
    val companyId: String,
    val archived: Boolean = false,
)
