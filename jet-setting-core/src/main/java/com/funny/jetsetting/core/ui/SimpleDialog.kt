package com.funny.jetsetting.core.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState

@Composable
fun SimpleDialog(
    openDialog: MutableState<Boolean>,
    title: String? = null,
    message: String = "message",
    confirmButtonAction: () -> Unit? = {},
    confirmButtonText : String = "确认",
    dismissButtonAction: () -> Unit? = {},
    dismissButtonText : String = "取消",
) {
    val emptyAction = {}
    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = {
                // Dismiss the dialog when the user clicks outside the dialog or on the back
                // button. If you want to disable that functionality, simply use an empty
                // onCloseRequest.
                openDialog.value = false
            },
            title = {
                if (title != null) Text(text = title)
            },
            text = {
                Text(message)
            },
            confirmButton = {
                if(confirmButtonAction!=emptyAction)
                Button(
                    onClick = {
                        openDialog.value = false
                        confirmButtonAction()
                    }) {
                    Text(confirmButtonText)
                }
            },
            dismissButton = {
                if(dismissButtonText.isNotEmpty())
                Button(
                    onClick = {
                        openDialog.value = false
                        dismissButtonAction()
                    }) {
                    Text(dismissButtonText)
                }
            }
        )
    }
}