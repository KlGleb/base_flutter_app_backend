package at.gleb.reviewmagic


import at.gleb.reviewmagic.auth.AuthInteractor
import at.gleb.reviewmagic.auth.UserDataSource
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


fun Application.checkIsDev() = if (!isDev()) throw Exception() else {
}

fun Application.isDev(): Boolean {
    if (getEnv("ktor.mongodb.db_name")
            .contains("dev") && getEnv("ktor.settings.dev") == "1"
    ) return true

    return false
}

