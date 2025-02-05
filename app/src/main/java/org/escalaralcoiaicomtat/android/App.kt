package org.escalaralcoiaicomtat.android

import android.app.Application
import io.sentry.Sentry
import io.sentry.android.core.SentryAndroid
import io.sentry.protocol.User
import java.util.UUID
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
            var deviceId = Preferences.getDeviceId(this@App).firstOrNull()
            val errorCollection = Preferences.hasOptedInForErrorCollection(this@App).firstOrNull()
            val performanceMetrics = Preferences.hasOptedInForPerformanceMetrics(this@App).firstOrNull()

            if (errorCollection != false) {
                // If there's no device id available, generate one and store it
                if (deviceId == null) {
                    val newDeviceId = UUID.randomUUID().toString()
                    Preferences.setDeviceId(this@App, newDeviceId)
                    deviceId = newDeviceId
                }

                SentryAndroid.init(this@App) { options ->
                    options.dsn = BuildConfig.SENTRY_DSN
                    // Only enable for production builds
                    options.isEnabled = BuildConfig.DEBUG
                    // Enable performance metrics if opted in
                    options.tracesSampleRate = if (performanceMetrics == false) 0.0 else 1.0

                    // Set the device id as the id of the user
                    Sentry.setUser(User().apply { id = deviceId })
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
