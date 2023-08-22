package org.escalaralcoiaicomtat.android

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.escalaralcoiaicomtat.android.network.NetworkObserver
import org.escalaralcoiaicomtat.android.storage.Preferences
import org.escalaralcoiaicomtat.android.worker.SyncWorker
import timber.log.Timber

class App: Application() {
    private val networkObserver by lazy { NetworkObserver.getInstance(this) }

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        networkObserver.startListening()

        CoroutineScope(Dispatchers.IO).launch {
            Preferences.hasEverSynchronized(applicationContext).collect { synchronized ->
                if (!synchronized) {
                    SyncWorker.synchronize(applicationContext)
                }
            }

            SyncWorker.schedule(applicationContext)
        }
    }

    override fun onTerminate() {
        super.onTerminate()

        networkObserver.stopListening()
    }
}
