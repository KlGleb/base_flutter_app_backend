package at.gleb.reviewmagic.auth

import at.gleb.reviewmagic.Cols
import at.gleb.reviewmagic.data.dto.UserDto
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import kotlinx.coroutines.flow.firstOrNull

class UserDataSource(private val cols: Cols) {
    suspend fun getById(id: String) = cols.users.find(Filters.eq(UserDto::id.name, id)).firstOrNull()

    suspend fun add(userDto: UserDto) {
        cols.users.insertOne(userDto)
    }

    suspend fun getByEmail(email: String) =
        cols.users.find(Filters.eq(UserDto::email.name, email)).firstOrNull()

    suspend fun setPassword(email: String, password: String): Boolean {
        val res =
            cols.users.updateOne(Filters.eq(UserDto::email.name, email), Updates.set(UserDto::password.name, password))
        return res.modifiedCount == 1L
    }
}