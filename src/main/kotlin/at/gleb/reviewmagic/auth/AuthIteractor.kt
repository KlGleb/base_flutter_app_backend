package at.gleb.reviewmagic.auth

import at.gleb.reviewmagic.Cols
import at.gleb.reviewmagic.data.dto.UserDto
import at.gleb.reviewmagic.exceptions.EmailIsAlreadyRegisteredException
import at.gleb.reviewmagic.exceptions.EmailNotRegistered
import at.gleb.reviewmagic.exceptions.WrongCode
import at.gleb.reviewmagic.exceptions.WrongCredentialsException
import at.gleb.reviewmagic.utils.hashPassword
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.jetbrains.annotations.VisibleForTesting
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.setValue
import java.util.*

const val ACCESS_TOKEN_EXPIRES_TIME_MILLS = 1000 * 60 * 60 * 24 * 7 //7 days
const val EMAIL_CODE_EXPIRES_TIME_MILLS = 1000 * 60 * 60 * 24 * 7 //7 days

class AuthInteractor(private val cols: Cols) {

    fun register(registerInput: RegisterInput, audience: String, issuer: String, secret: String): String {
        if (cols.users.findOne(UserDto::email eq registerInput.email) != null) {
            throw EmailIsAlreadyRegisteredException("Email ${registerInput.email} is already signed up")
        }
        cols.users.insertOne(UserDto(email = registerInput.email, password = registerInput.password.hashPassword()))

        return createToken(registerInput.email, registerInput.password, audience, issuer, secret)
    }

    fun provideToken(loginInput: LoginInput, audience: String, issuer: String, secret: String): String =
        createToken(loginInput.email, loginInput.password, audience, issuer, secret)

    fun deleteUser(userDto: UserDto) {
        cols.users.deleteOne(UserDto::_id eq userDto._id)
    }

    private fun createToken(email: String, password: String, audience: String, issuer: String, secret: String): String {
        val userFromDb = cols.users.findOne(UserDto::email eq email) ?: throw WrongCredentialsException()

        if (userFromDb.password != password.hashPassword()) throw Exception()

        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("id", userFromDb._id)
            .withClaim("email", userFromDb.email)
            .withExpiresAt(Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRES_TIME_MILLS))
            .sign(Algorithm.HMAC256(secret))
    }

    @VisibleForTesting
    fun createResetPasswordToken(
        email: String,
        audience: String,
        issuer: String,
        secret: String
    ): String {
        val userFromDb = cols.users.findOne(UserDto::email eq email) ?: throw EmailNotRegistered()

        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("resetEmail", userFromDb.email)
            .withExpiresAt(Date(System.currentTimeMillis() + EMAIL_CODE_EXPIRES_TIME_MILLS))
            .sign(Algorithm.HMAC256(secret))
    }

    /**
     * Returns email from the token
     */
    @VisibleForTesting
    fun validateResetPasswordToken(
        token: String,
        audience: String,
        issuer: String,
        secret: String
    ): String {
        val verifier = JWT
            .require(Algorithm.HMAC256(secret))
            .withAudience(audience)
            .withIssuer(issuer)
            .build()

        try {
            // Декодируем токен
            val decodedJWT = verifier.verify(token)

            // Проверяем аудиторию, издателя и другие необходимые параметры
            val audienceFromToken: String = decodedJWT.audience[0] // Пример получения аудитории
            val issuerFromToken: String = decodedJWT.issuer // Пример получения издателя
            val emailFromToken: String =
                decodedJWT.getClaim("resetEmail").asString() // Пример получения значения из пользовательского claim

            if (audienceFromToken != audience) throw WrongCode()
            if (issuerFromToken != issuer) throw WrongCode()
            if (emailFromToken.isEmpty()) throw WrongCode()

            return emailFromToken
        } catch (e: Exception) {
            throw WrongCode()
        }
    }

    fun sendResetPasswordCode(email: String, audience: String, issuer: String, secret: String) {
        val token = createResetPasswordToken(email, audience, issuer, secret)
        //todo: send email with token
        println("http://0.0.0.0:8080/resetPassword?code=$token")
    }

    fun resetPassword(
        resetPasswordInput: ResetPasswordInput,
        audience: String,
        issuer: String,
        secret: String
    ): String {

        val email = validateResetPasswordToken(resetPasswordInput.code, audience, issuer, secret)
        val newPassword = resetPasswordInput.newPassword.hashPassword()
        cols.users.updateOne(
            UserDto::email eq email,
            setValue(UserDto::password, newPassword)
        )
        return provideToken(
            LoginInput(email, resetPasswordInput.newPassword),
            audience,
            issuer,
            secret
        )
    }


}

