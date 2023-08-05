package org.escalaralcoiaicomtat.android.utils

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

suspend fun <T> LiveData<T>.await(
    @WorkerThread logic: suspend (T) -> Boolean = { true }
): T = withContext(Dispatchers.Main.immediate) {
    suspendCancellableCoroutine { cont ->
        val observer = object : Observer<T> {
            override fun onChanged(value: T) {
                val observer = this
                CoroutineScope(Dispatchers.IO).launch {
                    if (logic(value)) {
                        withContext(Dispatchers.Main.immediate) {
                            removeObserver(observer)
                        }
                        cont.resume(value)
                    }
                }
            }
        }

        observeForever(observer)

        cont.invokeOnCancellation {
            removeObserver(observer)
        }
    }
}
