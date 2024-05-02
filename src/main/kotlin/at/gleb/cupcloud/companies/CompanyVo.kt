package at.gleb.cupcloud.companies

import at.gleb.cupcloud.data.dto.CompanyDto

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