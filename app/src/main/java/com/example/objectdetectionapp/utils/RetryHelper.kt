package com.example.objectdetectionapp.utils

import android.util.Log
import kotlinx.coroutines.delay

suspend fun <T> retryOperation(
    maxAttempts: Int = 3,
    delayMillis: Long = 1000L,
    operationName: String = "",
    block: suspend () -> T
): T {
    var lastError: Throwable? = null

    repeat(maxAttempts) { attempt ->
        try {
            return block()
        } catch (e: Exception) {
            lastError = e
            Log.w("RetryHelper", "Attempt ${attempt + 1} failed for $operationName: ${e.message}")
            delay(delayMillis)
        }
    }

    throw lastError ?: IllegalStateException("Unknown error occurred in $operationName")
}