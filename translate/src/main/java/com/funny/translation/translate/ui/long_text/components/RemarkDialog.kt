package com.funny.translation.translate.ui.long_text.components

import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.funny.jetsetting.core.ui.SimpleDialog
import com.funny.translation.debug.rememberStateOf
import com.funny.translation.translate.R

@Composable
fun RemarkDialog(
    showState: MutableState<Boolean>,
    taskId: String,
    initialRemark: String,
    updateRemarkAction: (taskId: String, remark: String) -> Unit
) {
    // 更改备注
    var remark by rememberStateOf(value = initialRemark)
    SimpleDialog(
        openDialogState = showState,
        title = stringResource(id = R.string.change_remark),
        content = {
            TextField(
                value = remark,
                onValueChange = { remark = it },
                placeholder = {
                    Text(stringResource(id = R.string.remark))
                },
                maxLines = 1,
                singleLine = true
            )
        },
        confirmButtonAction = {
            updateRemarkAction(taskId, remark)
        },
        confirmButtonText = stringResource(id = com.funny.trans.login.R.string.confirm_to_modify),
    )
}