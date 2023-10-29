package at.gleb.reviewmagic.companies

import at.gleb.reviewmagic.Cols
import at.gleb.reviewmagic.data.dto.CompanyDto
import at.gleb.reviewmagic.data.dto.UserDto
import at.gleb.reviewmagic.exceptions.NotFound
import com.mongodb.client.model.Filters
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList

class CompanyInteractor(private val cols: Cols) {
    suspend fun getCompanies(user: UserDto) =
        cols.companies.find(Filters.eq(CompanyDto::userId.name, user.id!!)).toList()


    suspend fun createCompany(user: UserDto, it: CompanyInput): CompanyVo {
        val company = CompanyDto(userId = user.id?.toString()!!, name = it.name)
        cols.companies.insertOne(company)
        return company.vo
    }

    suspend fun editCompany(user: UserDto, it: CompanyInput, companyId: String): CompanyVo {
        val filter = Filters.and(
            Filters.eq(CompanyDto::_id.name, companyId),
            Filters.eq(CompanyDto::userId.name, user.id),
        )

        val oldCompany = cols.companies.find(filter).firstOrNull() ?: throw NotFound()



        /*cols.companies.updateOne(
           filter,
            oldCompany
        )*/
        TODO("Not yet implemented")
    }

    fun archiveCompany(user: UserDto, companyId: String): Boolean {
        TODO("Not yet implemented")
    }

    fun getCompanyById(user: UserDto, companyId: String): Any {
        TODO("Not yet implemented")
    }
}

private fun CompanyInput.toDto(userId: String) = CompanyDto(
    name = name,
    userId = userId
)
