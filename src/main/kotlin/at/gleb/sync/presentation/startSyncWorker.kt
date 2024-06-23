package at.gleb.sync.presentation

import at.gleb.sync.domain.SyncInteractor
import io.ktor.util.logging.*
import kotlinx.coroutines.*
import org.koin.java.KoinJavaComponent

const val UPDATE_DELAY_MILLS: Long = 1 * 1000 * 60 //1 minute

private val syncInteractor by KoinJavaComponent.inject<SyncInteractor>(SyncInteractor::class.java)
private val logger = KtorSimpleLogger("startSyncWorker")

@OptIn(DelicateCoroutinesApi::class)
fun startSyncWorker() {
    GlobalScope.launch(Dispatchers.IO) {
        while (isActive) {
            withTimeout(5000) {
                try {
                    syncInteractor.sync()
                } catch (e: Exception) {
                    logger.error(e)
                }
            }

            delay(UPDATE_DELAY_MILLS)
        }
    }
}