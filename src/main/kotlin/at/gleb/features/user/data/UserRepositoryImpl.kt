package at.gleb.features.user.data

import at.gleb.features.user.domain.UserRepository
import at.gleb.features.user.domain.vo.UserVo

class UserRepositoryImpl(private val userDataSource: UserDataSource) : UserRepository {
    override suspend fun getUserById(id: String): UserVo = userDataSource.getById(id)!!.vo

    override suspend fun getAllUsers(): List<UserVo> = userDataSource.getAll().map { it.vo }

    override suspend fun getUserByEmail(email: String): UserVo = userDataSource.getByEmail(email)!!.vo
}

val UserDto.vo: UserVo
    get() = UserVo(id = this.id.toString(), email = this.email)