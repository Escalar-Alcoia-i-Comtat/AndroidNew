package org.escalaralcoiaicomtat.android

import android.app.Application
import io.sentry.android.core.SentryAndroid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.escalaralcoiaicomtat.android.network.NetworkObserver
import org.escalaralcoiaicomtat.android.storage.Preferences
import org.escalaralcoiaicomtat.android.worker.SyncWorker
import timber.log.Timber

class App: Application() {
    private val networkObserver by lazy { NetworkObserver.getInstance(this) }

    override fun onCreate() {
        super.onCreate()

        // Enable Sentry conditionally
        CoroutineScope(Dispatchers.IO).launch {
            val errorCollection = Preferences.hasOptedInForErrorCollection(this@App).firstOrNull()
            val performanceMetrics = Preferences.hasOptedInForPerformanceMetrics(this@App).firstOrNull()

            if (errorCollection != false) {
                SentryAndroid.init(this@App) { options ->
                    options.dsn = BuildConfig.SENTRY_DSN
                    // Only enable for production builds
                    options.isEnabled = BuildConfig.DEBUG
                    // Enable performance metrics if opted in
                    options.enableTracing = performanceMetrics != false
                }
            }
        }

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
