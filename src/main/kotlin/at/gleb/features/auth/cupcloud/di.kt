package at.gleb.features.auth.cupcloud


import at.gleb.auth.AuthInteractor
import at.gleb.counterparty.data.CounterpartyDbDataSource
import at.gleb.features.user.data.UserDataSource
import at.gleb.features.user.data.UserRepositoryImpl
import at.gleb.features.user.domain.UserRepository
import at.gleb.notifications.MailNotificator
import at.gleb.notifications.NotificatorScheduler
import at.gleb.notifications.NotificatorSchedulerImpl
import at.gleb.notifications.PushNotificator
import at.gleb.sync.data.SyncDataSource
import at.gleb.sync.data.SyncDataSourceMockImpl
import at.gleb.sync.domain.SyncInteractor
import com.google.gson.Gson
import com.mongodb.kotlin.client.coroutine.MongoClient
import io.ktor.server.application.*
import org.koin.core.module.Module
import org.koin.dsl.module
import org.simplejavamail.api.mailer.config.TransportStrategy
import org.simplejavamail.mailer.MailerBuilder

fun Application.appModule(): Module = module {
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
        AuthInteractor(get(), get())
    }

    single {
        Gson()
    }

    single<SyncDataSource> {
        SyncDataSourceMockImpl()
    }

    single { CounterpartyDbDataSource(get()) }
    single<NotificatorScheduler> { NotificatorSchedulerImpl() }
    single { UserDataSource(get()) }
    single { SyncInteractor(get(), get(), get(), get()) }
    single { MailNotificator(get(), getEnv("ktor.smtp.from")) }
    single { PushNotificator() }

    single {
        val server = getEnv("ktor.smtp.server")
        val port = getEnv("ktor.smtp.port")
        val login = getEnv("ktor.smtp.login")
        val password = getEnv("ktor.smtp.password")
        MailerBuilder
            .withTransportStrategy(TransportStrategy.SMTP_TLS)
            .withDebugLogging(true)
            .withSMTPServer(server, port.toInt(), login, password)
            .buildMailer()
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

