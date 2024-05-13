package at.gleb.features.auth.cupcloud


import at.gleb.auth.AuthInteractor
import at.gleb.features.user.data.UserDataSource
import at.gleb.features.user.data.UserRepositoryImpl
import at.gleb.features.user.domain.UserRepository
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.turbo.MarkerFilter
import com.mongodb.kotlin.client.coroutine.MongoClient
import io.ktor.server.application.*
import org.koin.core.module.Module
import org.koin.dsl.module
import org.slf4j.LoggerFactory


fun Application.appModule(): Module {
    return module {
        single {
            val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
            loggerContext.getLogger("org.mongodb.driver").level = Level.OFF
            loggerContext.getLogger("org.mongodb.driver.cluster").level = Level.OFF
            loggerContext.getLogger("org.mongodb.driver.connection").level = Level.OFF
            loggerContext.getLogger("org.mongodb.driver.operation").level = Level.OFF
            loggerContext.getLogger("org.mongodb.driver.protocol.command").apply {
                this.loggerContext.addTurboFilter(MarkerFilter().apply {
                    this.setMarker("Sending command")
                })
            }

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

