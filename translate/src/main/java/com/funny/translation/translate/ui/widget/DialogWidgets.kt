package com.funny.translation.translate.ui.widget

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*

val emptyAction = {}

@Composable
fun SimpleDialog(
    openDialog: Boolean,
    updateOpenDialog: (Boolean) -> Unit,
    title: String? = null,
    message: String = "message",
    confirmButtonAction: (() -> Unit)? = {},
    confirmButtonText : String = "确认",
    dismissButtonAction: (() -> Unit)? = {},
    dismissButtonText : String = "取消",
    userData : Any? = null,
    closeable : Boolean = true
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
                if(confirmButtonAction!=emptyAction) {
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
    confirmButtonText : String = "确认",
    dismissButtonAction: (() -> Unit)? = {},
    dismissButtonText : String = "取消",
    userData : Any? = null,
    closeable : Boolean = true
) {
    val (openDialog, updateOpenDialog) = openDialogState
    SimpleDialog(openDialog, updateOpenDialog, title, message, confirmButtonAction, confirmButtonText, dismissButtonAction, dismissButtonText, userData, closeable)
}