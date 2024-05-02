package at.gleb.cupcloud.auth

import at.gleb.cupcloud.data.dto.UserDto
import at.gleb.cupcloud.exceptions.EmailIsAlreadyRegisteredException
import at.gleb.cupcloud.exceptions.EmailNotRegistered
import at.gleb.cupcloud.exceptions.WrongCode
import at.gleb.cupcloud.exceptions.WrongCredentialsException
import at.gleb.cupcloud.utils.hashPassword
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.jetbrains.annotations.VisibleForTesting
import java.util.*

const val ACCESS_TOKEN_EXPIRES_TIME_MILLS = 1000 * 60 * 60 * 24 * 7 //7 days
const val EMAIL_CODE_EXPIRES_TIME_MILLS = 1000 * 60 * 60 * 24 * 7 //7 days

class AuthInteractor(private val userDataSource: UserDataSource) {

    suspend fun register(registerInput: RegisterInput, audience: String, issuer: String, secret: String): String {
        if (userDataSource.getByEmail(registerInput.email) != null) {
            throw EmailIsAlreadyRegisteredException("Email ${registerInput.email} is already signed up")
        }

        val userDto = UserDto(email = registerInput.email, password = registerInput.password.hashPassword())
        userDataSource.add(userDto)

        return createToken(registerInput.email, registerInput.password, audience, issuer, secret)
    }

    suspend fun provideToken(loginInput: LoginInput, audience: String, issuer: String, secret: String): String =
        createToken(loginInput.email, loginInput.password, audience, issuer, secret)

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
        } catch (e: Exception) {
            throw WrongCode()
        }
    }

    suspend fun sendResetPasswordCode(email: String, audience: String, issuer: String, secret: String) {
        val token = createResetPasswordToken(email, audience, issuer, secret)
        //todo: send email with token
        println("http://0.0.0.0:8080/resetPassword?code=$token")
    }

    suspend fun resetPassword(
        resetPasswordInput: ResetPasswordInput,
        audience: String,
        issuer: String,
        secret: String
    ): String {
        val email = validateResetPasswordToken(resetPasswordInput.code, audience, issuer, secret)
        val newPassword = resetPasswordInput.newPassword.hashPassword()
        userDataSource.setPassword(email, newPassword)
        return provideToken(
            LoginInput(email, resetPasswordInput.newPassword),
            audience,
            issuer,
            secret
        )
    }


}

