package com.funny.translation.codeeditor.ui.base

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ExpandableDropdownItem(
    text: String,
    requestDismiss: () -> Unit,
    dropDownItems: @Composable () -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }
    DropdownMenuItem(onClick = {
        expanded = true
        requestDismiss()
    }) {
        Text(text = text)
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            dropDownItems()
        }
    }

}

@Composable
fun SimpleDialog(
    openDialog: MutableState<Boolean>,
    title: String? = null,
    message: String = "message",
    confirmButtonAction: () -> Unit? = {},
    confirmButtonText : String = "确认",
    dismissButtonAction: () -> Unit? = {},
    dismissButtonText : String = "取消"
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

@Composable
fun ComposeSpinner(
    data : List<String>,
    initialData : String,
    label : String,
    selectAction : (Int)->Unit
) {
    val text = remember { mutableStateOf(initialData) } // initial value
    val isOpen = remember { mutableStateOf(false) } // initial value
    val openCloseOfDropDownList: (Boolean) -> Unit = {
        isOpen.value = it
    }
    val userSelectedString: (String) -> Unit = {
        text.value = it
    }
    Box {
        Column {
            OutlinedTextField(
                value = text.value,
                onValueChange = { text.value = it },
                label = { Text(text = label) },
                modifier = Modifier.fillMaxWidth()
            )
            DropDownList(
                requestToOpen = isOpen.value,
                list = data,
                openCloseOfDropDownList,
                { text , index ->
                    userSelectedString(text)
                    selectAction(index)
                }
            )
        }
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Transparent)
                .padding(10.dp)
                .clickable(
                    onClick = { isOpen.value = true }
                )
        )
    }
}

@Composable
fun DropDownList(
    requestToOpen: Boolean = false,
    list: List<String>,
    request: (Boolean) -> Unit,
    selectedString: (String , Int) -> Unit
) {
    DropdownMenu(
        modifier = Modifier.fillMaxWidth(),
        expanded = requestToOpen,
        onDismissRequest = { request(false) },
    ) {
        for(index in list.indices){
            val curValue = list[index]
            DropdownMenuItem(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    request(false)
                    selectedString(curValue , index)
                }
            ) {
                Text(text = curValue, modifier = Modifier.wrapContentWidth())
            }
        }
    }
}