package org.escalaralcoiaicomtat.android.utils

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.work.DirectExecutor
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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

@Suppress("BlockingMethodInNonBlockingContext")
suspend inline fun <R> ListenableFuture<R>.await(): R {
    // Fast path
    if (isDone) {
        try {
            return get()
        } catch (e: ExecutionException) {
            throw e.cause ?: e
        }
    }
    return suspendCancellableCoroutine { cancellableContinuation ->
        addListener(
            {
                try {
                    cancellableContinuation.resume(get())
                } catch (throwable: Throwable) {
                    val cause = throwable.cause ?: throwable
                    when (throwable) {
                        is CancellationException -> cancellableContinuation.cancel(cause)
                        else -> cancellableContinuation.resumeWithException(cause)
                    }
                }
            },
            DirectExecutor.INSTANCE
        )

        cancellableContinuation.invokeOnCancellation {
            cancel(false)
        }
    }
}
