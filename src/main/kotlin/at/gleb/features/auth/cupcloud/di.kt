package at.gleb.features.auth.cupcloud


import at.gleb.auth.AuthInteractor
import at.gleb.features.user.data.UserDataSource
import at.gleb.features.user.data.UserRepositoryImpl
import at.gleb.features.user.domain.UserRepository
import com.google.gson.Gson
import com.mongodb.kotlin.client.coroutine.MongoClient
import io.ktor.server.application.*
import org.koin.core.module.Module
import org.koin.dsl.module


fun Application.appModule(): Module {
    return module {
        single {
            val mongoDbUrl = getEnv("ktor.mongodb.db_url")
            MongoClient.create(mongoDbUrl)

        }
        single {
            val dbName = getEnv("ktor.mongodb.db_name")
            get<MongoClient>().getDatabase(dbName)
        }

        single {
            Cols(get())
        }

        single {
            UserDataSource(get())
        }

        single<UserRepository> {
            UserRepositoryImpl(get())
        }

        single {
            AuthInteractor(get())
        }

        single {
            Gson()
        }

        /*single {
            jacksonObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.ALWAYS)
                .registerKotlinModule()
                .registerModule(IdJacksonModule())
        }

        single(named(NON_NULL_JACKSON)) {
            jacksonObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .registerKotlinModule()
                .registerModule(IdJacksonModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }*/
    }
}


fun Application.getEnv(name: String) = environment.config.propertyOrNull(name)?.getString()!!

@Suppress("unused")
fun Application.checkIsDev() {
    if (!isDev()) throw Exception()
}

fun Application.isDev(): Boolean {
    if (getEnv("ktor.mongodb.db_name")
            .contains("dev") && getEnv("ktor.settings.dev") == "1"
    ) return true

    return false
}

