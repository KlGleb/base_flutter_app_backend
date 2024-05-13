package at.gleb.features.user.data

import at.gleb.features.auth.cupcloud.Cols
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

class UserDataSource(private val cols: Cols) {
    suspend fun getById(id: String) = cols.users.find(Filters.eq("_id", ObjectId(id))).firstOrNull()
    suspend fun add(userDto: UserDto) {
        cols.users.insertOne(userDto)
    }

    suspend fun getByEmail(email: String) = cols.users.find(Filters.eq(UserDto::email.name, email)).firstOrNull()

    suspend fun setPassword(email: String, password: String): Boolean {
        val res =
            cols.users.updateOne(Filters.eq(UserDto::email.name, email), Updates.set(UserDto::password.name, password))
        return res.modifiedCount == 1L
    }

    suspend fun getAll(): List<UserDto> = cols.users.find<UserDto>().toList()

    suspend fun addResetPasswordCode(email: String, token: String): String {
        val code = getRandomString()
        val codeDto = OneTimeCodeDto(token = token, key = code)
        cols.users.updateOne(
            Filters.eq(UserDto::email.name, email),
            Updates.set(UserDto::resetPasswordCode.name, codeDto)
        )
        return code
    }

    private fun getRandomString(length: Int = 6): String {
        val allowedChars = ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}