package at.gleb

import at.gleb.features.auth.cupcloud.Cols
import at.gleb.features.auth.cupcloud.utils.getRandomString
import at.gleb.features.auth.cupcloud.utils.hashPassword
import at.gleb.features.user.data.UserDto
import com.google.gson.Gson
import com.mongodb.client.model.Filters
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import org.koin.java.KoinJavaComponent.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class ApplicationTest {
    @Test
    fun registration() = testApplication {
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

        data class User(val id: ObjectId, val email: String)

        val obj2: User = testResp2.toObj(User::class.java)

        assertEquals(obj2.email, email)
        val cols: Cols by inject(Cols::class.java)
        cols.users.deleteOne(Filters.eq(UserDto::id.name, obj2.id))

    }

    @Test
    fun login() = testApplication {
        val email = getRandomString(15) + "_test@gleb.at"
        val password = getRandomString(14)

        application {
            runBlocking {
                val user = UserDto(email = email, password = password.hashPassword())
                val cols: Cols by inject(Cols::class.java)
                cols.users.insertOne(user)
            }
        }

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