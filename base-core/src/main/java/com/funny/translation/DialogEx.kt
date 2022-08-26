package com.funny.translation

import android.app.AlertDialog
import android.content.Context
import com.funny.translation.helper.handler.runOnUI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun awaitDialog(
    context: Context,
    title: String,
    message: String,
    positiveButton: String,
    negativeButton: String,
) = suspendCancellableCoroutine<Boolean> { continuation ->
     runOnUI {
        AlertDialog.Builder(context).setTitle(title).setMessage(message)
            .setPositiveButton(positiveButton) { _, _ ->
                continuation.resume(true, {})
            }
            .setNegativeButton(negativeButton) { _, _ ->
                continuation.resume(false, {})
            }
            .show()
    }
}