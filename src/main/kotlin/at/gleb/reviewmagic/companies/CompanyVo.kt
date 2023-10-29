package at.gleb.reviewmagic.companies

import at.gleb.reviewmagic.data.dto.CompanyDto

data class CompanyVo(
    val id: String,
    val name: String,
    val userId: String,
)

val CompanyDto.vo
    get() = CompanyVo(
        id = _id!!,
        name = name,
        userId =  userId,
    )