package com.funny.jetsetting.core.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import com.funny.jetsetting.core.R

@Composable
fun SimpleDialog(
    openDialog: Boolean,
    updateOpenDialog: (Boolean) -> Unit,
    title: String? = null,
    message: String = "message",
    confirmButtonAction: (() -> Unit)? = null,
    confirmButtonText : String = stringResource(R.string.confirm),
    dismissButtonAction: (() -> Unit)? = null,
    dismissButtonText : String = stringResource(R.string.cancel),
    closeable: Boolean = true
) {

    if (openDialog) {
        AlertDialog(
            onDismissRequest = {
                // Dismiss the dialog when the user clicks outside the dialog or on the back
                // button. If you want to disable that functionality, simply use an empty
                // onCloseRequest.
                if (closeable) updateOpenDialog(false)
            },
            title = {
                if (title != null) Text(text = title)
            },
            text = {
                Text(message)
            },
            confirmButton = {
                if(confirmButtonText.isNotEmpty()) {
                    Button(
                        onClick = {
                            updateOpenDialog(false)
                            confirmButtonAction?.invoke()
                        }) {
                        Text(confirmButtonText)
                    }
                }
            },
            dismissButton = {
                if(dismissButtonText.isNotEmpty()) {
                    Button(
                        onClick = {
                            updateOpenDialog(false)
                            dismissButtonAction?.invoke()
                        }) {
                        Text(dismissButtonText)
                    }
                }
            }
        )
    }
}

@Composable
fun SimpleDialog(
    openDialogState: MutableState<Boolean>,
    title: String? = null,
    message: String = "message",
    confirmButtonAction: (() -> Unit)? = {},
    confirmButtonText : String = stringResource(R.string.confirm),
    dismissButtonAction: (() -> Unit)? = {},
    dismissButtonText : String = stringResource(R.string.cancel),
    closeable: Boolean = true
) {
    val (openDialog, updateOpenDialog) = openDialogState
    SimpleDialog(openDialog, updateOpenDialog, title, message, confirmButtonAction, confirmButtonText, dismissButtonAction, dismissButtonText, closeable)
}