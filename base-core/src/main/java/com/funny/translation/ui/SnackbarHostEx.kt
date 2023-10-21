package com.funny.translation.ui

import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import com.funny.translation.helper.SimpleAction

private const val TAG = "SnackbarHostEx"

suspend fun SnackbarHostState.showSnackbar(
    message: String,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
    duration: SnackbarDuration =
        if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Indefinite,
    onClick: SimpleAction = {}
): SnackbarResult {
    val res = showSnackbar(message, actionLabel, withDismissAction, duration)
    Log.d(TAG, "showSnackbar: res: $res")
    if (res == SnackbarResult.ActionPerformed) onClick()
    return res
}