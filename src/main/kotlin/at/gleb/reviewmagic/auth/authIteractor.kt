package at.gleb.reviewmagic.auth

import at.gleb.reviewmagic.Cols
import at.gleb.reviewmagic.data.dto.UserDto
import at.gleb.reviewmagic.utils.hashPassword
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import java.util.*


fun RegisterInput.register(audience: String, issuer: String, secret: String): String {
    Cols.users.insertOne(UserDto(email = email, password = password.hashPassword()))

    return createToken(email, password, audience, issuer, secret)
}

fun LoginInput.provideToken(audience: String, issuer: String, secret: String): String =
    createToken(email, password, audience, issuer, secret)

fun deleteUser(userDto: UserDto) {
    Cols.users.deleteOne(UserDto::_id eq userDto._id)
}

private fun createToken(email: String, password: String, audience: String, issuer: String, secret: String): String {
    val userFromDb = Cols.users.findOne(UserDto::email eq email)!!

    if (userFromDb.password != password.hashPassword()) throw Exception()

    return JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim("id", userFromDb._id)
        .withClaim("email", userFromDb.email)
        .withExpiresAt(Date(System.currentTimeMillis() + 60000))
        .sign(Algorithm.HMAC256(secret))
}

