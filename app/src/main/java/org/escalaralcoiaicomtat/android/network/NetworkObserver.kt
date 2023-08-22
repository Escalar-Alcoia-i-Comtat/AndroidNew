package org.escalaralcoiaicomtat.android.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.escalaralcoiaicomtat.android.BuildConfig
import java.net.UnknownHostException

class NetworkObserver private constructor(context: Context) {
    companion object {
        @Volatile
        private var instance: NetworkObserver? = null

        fun getInstance(context: Context) = instance ?: synchronized(this) {
            instance ?: NetworkObserver(context).also { instance = it }
        }

        @Composable
        fun rememberNetworkObserver(): NetworkObserver {
            val context = LocalContext.current
            return remember { getInstance(context) }
        }
    }

    private val connectivityManager = context.getSystemService(ConnectivityManager::class.java)

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)

            // Try pinging the backend to check if Internet connection is available
            try {
                network.getByName(BuildConfig.HOSTNAME)

                _isNetworkAvailable.postValue(true)
            } catch (_: UnknownHostException) {
                _isNetworkAvailable.postValue(false)
            }
        }

        override fun onLost(network: Network) {
            super.onLost(network)

            _isNetworkAvailable.postValue(false)
        }
    }

    private val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .build()

    private val _isNetworkAvailable = MutableLiveData(false)
    val isNetworkAvailable: LiveData<Boolean> get() = _isNetworkAvailable

    fun startListening() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        } else {
            connectivityManager.requestNetwork(networkRequest, networkCallback)
        }
    }

    fun stopListening() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}
