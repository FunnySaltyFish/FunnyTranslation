package com.funny.translation.translate.ui.long_text

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.funny.jetsetting.core.ui.SimpleDialog
import com.funny.translation.debug.rememberStateOf
import com.funny.translation.helper.DataHolder
import com.funny.translation.helper.SimpleAction
import com.funny.translation.helper.TimeUtils
import com.funny.translation.helper.string
import com.funny.translation.translate.LocalNavController
import com.funny.translation.translate.R
import com.funny.translation.translate.database.LongTextTransTaskMini
import com.funny.translation.translate.extentions.formatBraceStyle
import com.funny.translation.translate.ui.TranslateScreen
import com.funny.translation.translate.ui.long_text.components.RemarkDialog
import com.funny.translation.translate.ui.main.SwipeToDismissItem
import com.funny.translation.translate.ui.widget.CommonPage
import com.funny.translation.translate.ui.widget.HintText
import com.funny.translation.ui.FixedSizeIcon
import java.util.UUID

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LongTextTransListScreen() {
    val navController = LocalNavController.current
    val navigateToDetailPage = remember {
        { id: String?, text: String? ->
            // 如果 id 为 null，则为创建任务并导航过去
            if (id == null) {
                val newId = UUID.randomUUID().toString()
                DataHolder.put(newId, text)
                navController.navigate(
                    TranslateScreen.LongTextTransDetailScreen.route.formatBraceStyle(
                        "id" to newId
                    )
                )
            } else {
                navController.navigate(
                    TranslateScreen.LongTextTransDetailScreen.route.formatBraceStyle(
                        "id" to id
                    )
                )
            }
        }
    }
    val vm: LongTextTransListViewModel = viewModel()
    CommonPage(title = stringResource(id = R.string.long_text_trans)) {
        val list by vm.taskList.collectAsState(initial = emptyList())

        LazyColumn {
            if (list.isNotEmpty()) {
                items(list, key = { it.id }) {
                    LongTextTransItem(
                        modifier = Modifier.animateItemPlacement(),
                        task = it,
                        onClick = { navigateToDetailPage(it.id, null) },
                        deleteTaskAction = vm::deleteTask,
                        updateRemarkAction = vm::updateRemark
                    )
                }
                item {
                    HintText(text = stringResource(id = R.string.long_text_trans_list_tip))
                }
            } else {
                item {
                    HintText(text = stringResource(id = R.string.empty_long_text_trans_history))
                }
            }
        }
    }

    var showInputDialog by rememberStateOf(value = false)
    if (showInputDialog) {
        // 输入对话框
        var inputText by rememberStateOf(value = "")
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                TextButton(onClick = {
                    showInputDialog = false
                    if (inputText.isNotBlank()) {
                        navigateToDetailPage(null, inputText)
                    }
                }) {
                    Text(text = string(id = R.string.message_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showInputDialog = false }) {
                    Text(text = string(id = R.string.cancel))
                }
            },
            title = {
                Text(text = string(id = R.string.please_input))
            },
            text = {
                TextField(value = inputText, onValueChange = { inputText = it })
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LongTextTransItem(
    modifier: Modifier = Modifier,
    task: LongTextTransTaskMini,
    onClick: SimpleAction,
    deleteTaskAction: (LongTextTransTaskMini) -> Unit,
    updateRemarkAction: (taskId: String, String) -> Unit = { _, _ -> }
) {
    val oneLineText = @Composable { text: String ->
        Text(text = text, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
    val showDeleteDialog = rememberStateOf(value = false)
    SimpleDialog(
        openDialogState = showDeleteDialog,
        title = stringResource(id = R.string.confirm_to_delete),
        message = stringResource(id = R.string.long_text_trans_task_not_finished_tip),
        confirmButtonAction = {
            deleteTaskAction(task)
        }
    )

    val showChangeRemarkDialog = rememberStateOf(value = false)
    RemarkDialog(
        showState = showChangeRemarkDialog,
        taskId = task.id,
        initialRemark = task.remark,
        updateRemarkAction = updateRemarkAction
    )
    SwipeToDismissItem(
        modifier = modifier.fillMaxWidth(),
        onDismissed = {
            if (task.finishTranslating) {
                deleteTaskAction(task)
            } else {
                showDeleteDialog.value = true
            }
        }
    ) {
        ListItem(
            modifier = Modifier.combinedClickable(
                onClick = onClick, onLongClick = { showChangeRemarkDialog.value = true }
            ),
            headlineContent = {
                oneLineText(task.remark.ifEmpty { string(R.string.no_remark) })
            },
            supportingContent = {
                oneLineText(TimeUtils.formatTime(task.updateTime))
            },
            trailingContent = {
                if (task.finishTranslating) {
                    FixedSizeIcon(Icons.Filled.Done, contentDescription = "Translated")
                } else {
                    CircularProgressIndicator(progress = task.translatedProgress)
                }
            }
        )
    }
}