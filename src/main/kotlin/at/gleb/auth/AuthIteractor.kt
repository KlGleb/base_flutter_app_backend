package at.gleb.auth

import at.gleb.features.auth.cupcloud.exceptions.*
import at.gleb.features.auth.cupcloud.utils.hashPassword
import at.gleb.features.user.data.UserDataSource
import at.gleb.features.user.data.UserDto
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.TokenExpiredException
import org.jetbrains.annotations.VisibleForTesting
import java.util.*

const val ACCESS_TOKEN_EXPIRES_TIME_MILLS = 1000 * 60 * 60 * 24 * 7 //7 days
const val EMAIL_CODE_EXPIRES_TIME_MILLS = 1000 * 60 * 5 //5 min

class AuthInteractor(private val userDataSource: UserDataSource) {

    suspend fun register(email: String, password: String, audience: String, issuer: String, secret: String): String {
        if (userDataSource.getByEmail(email) != null) {
            throw EmailIsAlreadyRegisteredException("Email $email is already signed up")
        }

        val userDto = UserDto(email = email, password = password.hashPassword())
        userDataSource.add(userDto)

        return createToken(email, password, audience, issuer, secret)
    }

    suspend fun provideToken(
        email: String,
        password: String,
        audience: String,
        issuer: String,
        secret: String
    ): String = createToken(email, password, audience, issuer, secret)

    suspend fun sendResetPasswordCode(email: String, audience: String, issuer: String, secret: String) {
        val token = createResetPasswordToken(email, audience, issuer, secret)
        val code = userDataSource.addResetPasswordCode(email, token)
        println("code: $code")
    }

    private suspend fun createToken(
        email: String,
        password: String,
        audience: String,
        issuer: String,
        secret: String
    ): String {
        val userFromDb = userDataSource.getByEmail(email = email) ?: throw WrongCredentialsException()

        if (userFromDb.password != password.hashPassword()) throw WrongCredentialsException()

        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("id", userFromDb.id!!.toString())
            .withClaim("email", userFromDb.email)
            .withExpiresAt(Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRES_TIME_MILLS))
            .sign(Algorithm.HMAC256(secret))
    }

    @VisibleForTesting
    suspend fun createResetPasswordToken(
        email: String,
        audience: String,
        issuer: String,
        secret: String
    ): String {
        val userFromDb = userDataSource.getByEmail(email) ?: throw EmailNotRegistered()

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
        } catch (e: TokenExpiredException) {
            throw CodeExpired()
        } catch (e: Exception) {
            throw WrongCode()
        }
    }

    suspend fun resetPassword(
        email: String,
        newPassword: String,
        code: String,
        audience: String,
        issuer: String,
        secret: String
    ): String {
        val codeDto = userDataSource.getByEmail(email)?.resetPasswordCode ?: throw NotFound()
        if (codeDto.key != code) throw WrongCode()

        validateResetPasswordToken(codeDto.token, audience, issuer, secret)
        val passHashed = newPassword.hashPassword()
        userDataSource.setPassword(email, passHashed)
        return provideToken(
            email,
            newPassword,
            audience,
            issuer,
            secret
        )
    }

}

