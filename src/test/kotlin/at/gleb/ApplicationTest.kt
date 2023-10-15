package at.gleb

import at.gleb.reviewmagic.Cols
import at.gleb.reviewmagic.data.dto.UserDto
import at.gleb.reviewmagic.utils.getRandomString
import at.gleb.reviewmagic.utils.hashPassword
import com.google.gson.Gson
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.litote.kmongo.eq
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class ApplicationTest {

    @Test
    fun registration() = testApplication {
        application {
        }

        val email = getRandomString(15) + "_test@gleb.at"
        val password = getRandomString(14)

        val regResponse = client.post("/auth/register") {
            setBody("{'email':'$email','password':'$password'}")
            headers {
                append(HttpHeaders.ContentType, "application/json")
            }
        }

        assertEquals(HttpStatusCode.OK, regResponse.status)

        data class RespClass(val token: String)

        val obj: RespClass = regResponse.toObj(RespClass::class.java)
        val token = obj.token

        assertNotNull(token)

        val testResp = client.get("/auth/test") {
            headers {
                append(HttpHeaders.ContentType, "application/json")
            }
        }

        assertNotEquals(HttpStatusCode.OK, testResp.status)

        val testResp2 = client.get("/auth/test") {
            headers {
                append(HttpHeaders.ContentType, "application/json")
                bearerAuth(token)
            }
        }

        data class User(val _id: String, val email: String)

        val obj2: User = testResp2.toObj(User::class.java)

        assertEquals(obj2.email, email)

        Cols.users.deleteOne(UserDto::_id eq obj2._id)

    }

    @Test
    fun login() = testApplication {
        application {
        }
        val email = getRandomString(15) + "_test@gleb.at"
        val password = getRandomString(14)

        val user = UserDto(email = email, password = password.hashPassword())
        Cols.users.insertOne(user)


        client.post("/auth/login") {
            setBody("{'email':'$email','password':'$password'}")
            headers {
                append(HttpHeaders.ContentType, "application/json")
            }
        }.apply {
            assertEquals(HttpStatusCode.OK, status)

            data class RespClass(val token: String)
            assertNotNull(toObj(RespClass::class.java).token)
        }

    }
}


suspend fun <T> HttpResponse.toObj(c: Class<T>): T {
    val gson = Gson()
    return gson.fromJson(bodyAsText(), c)
}