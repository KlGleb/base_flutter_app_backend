package at.gleb

import at.gleb.reviewmagic.Cols
import at.gleb.reviewmagic.auth.AuthInteractor
import at.gleb.reviewmagic.auth.LoginInput
import at.gleb.reviewmagic.auth.UserDataSource
import at.gleb.reviewmagic.data.dto.UserDto
import at.gleb.reviewmagic.exceptions.WrongCode
import at.gleb.reviewmagic.utils.hashPassword
import com.mongodb.kotlin.client.coroutine.MongoCollection
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class ResetPasswordTests {

    private val email = "test@example.com"
    private val audience = "testAudience"
    private val issuer = "testIssuer"
    private val secret = "testSecret"
    private val id = ObjectId()

    private val db = mockk<Cols>()
    private val collection = mockk<MongoCollection<UserDto>>()
    private val dataSource = mockk<UserDataSource>()
    private val userDto = UserDto(id, email, "".hashPassword())
    lateinit var interactor: AuthInteractor

    @Before
    fun setUpAll() {
//        every { collection.findOne(UserDto::email eq email) } returns userDto
        coEvery { dataSource.setPassword(email, any()) } returns true
        coEvery { dataSource.getById(id.toString()) } returns userDto
        coEvery { dataSource.add(userDto) } returns Unit
        coEvery { dataSource.getByEmail(email) } returns userDto


        every { db.users } returns collection
        interactor = AuthInteractor(dataSource)
    }

    @Test
    fun `validateResetPasswordToken should pass with a valid token`() = testApplication {
        application {
            runBlocking {
                val token = interactor.createResetPasswordToken(email, audience, issuer, secret)
                interactor.validateResetPasswordToken(token, audience, issuer, secret)
            }
        }
    }

    @Test
    fun `validateResetPasswordToken should not pass with invalid token`() = testApplication {
        application {
            runBlocking {
                val token = interactor.createResetPasswordToken(email, "wrong audience", issuer, secret)
                assertThrows(WrongCode::class.java) {
                    interactor.validateResetPasswordToken(token, audience, issuer, secret)
                }
            }
        }

    }

    @Test
    fun `validateResetPasswordToken should not pass with access token`() = testApplication {
        application {
            runBlocking {
                val token = interactor.provideToken(LoginInput(email, ""), audience, issuer, secret)

                assertThrows(WrongCode::class.java) {
                    interactor.validateResetPasswordToken(token, audience, issuer, secret)
                }
            }
        }
    }
}
