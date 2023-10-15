package at.gleb.reviewmagic


import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.turbo.MarkerFilter
import com.mongodb.client.MongoClient
import io.ktor.server.application.*
import org.koin.core.module.Module
import org.koin.dsl.module
import org.litote.kmongo.KMongo
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
            KMongo.createClient(mongoDbUrl)
        }
        single {
            val dbName = getEnv("ktor.mongodb.db_name")
            get<MongoClient>().getDatabase(dbName)
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

