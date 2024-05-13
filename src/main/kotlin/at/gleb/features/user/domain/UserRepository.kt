package at.gleb.features.user.domain

import at.gleb.features.user.domain.vo.UserVo

interface UserRepository {
    suspend fun getUserById(id: String): UserVo
    suspend fun getAllUsers(): List<UserVo>
    suspend fun getUserByEmail(email: String): UserVo
}