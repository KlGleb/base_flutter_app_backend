package at.gleb

import at.gleb.reviewmagic.Cols
import at.gleb.reviewmagic.auth.AuthInteractor
import at.gleb.reviewmagic.auth.LoginInput
import at.gleb.reviewmagic.data.dto.UserDto
import at.gleb.reviewmagic.exceptions.WrongCode
import at.gleb.reviewmagic.utils.hashPassword
import com.mongodb.client.MongoCollection
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.litote.kmongo.eq
import org.litote.kmongo.findOne

class ResetPasswordTests {

    private val email = "test@example.com"
    private val audience = "testAudience"
    private val issuer = "testIssuer"
    private val secret = "testSecret"
    private val id = "testId"

    private val db = mockk<Cols>()
    private val collection = mockk<MongoCollection<UserDto>>()
    private val userDto = UserDto(id, email, "".hashPassword())
    lateinit var interactor: AuthInteractor

    @Before
    fun setUpAll() {
        every { collection.findOne(UserDto::email eq email) } returns userDto
        every { db.users } returns collection
        interactor = AuthInteractor(db)
    }

    @Test
    fun `validateResetPasswordToken should pass with a valid token`() = testApplication {
        application {
            val token = interactor.createResetPasswordToken(email, audience, issuer, secret)
            interactor.validateResetPasswordToken(token, audience, issuer, secret)
        }
    }

    @Test
    fun `validateResetPasswordToken should not pass with invalid token`() = testApplication {
        application {
            val token = interactor.createResetPasswordToken(email, "wrong audience", issuer, secret)

            assertThrows(WrongCode::class.java) {
                interactor.validateResetPasswordToken(token, audience, issuer, secret)
            }
        }

    }

    @Test
    fun `validateResetPasswordToken should not pass with access token`() = testApplication {
        application {
            val token = interactor.provideToken(LoginInput(email, ""), audience, issuer, secret)

            assertThrows(WrongCode::class.java) {
                interactor.validateResetPasswordToken(token, audience, issuer, secret)
            }
        }
    }
}
