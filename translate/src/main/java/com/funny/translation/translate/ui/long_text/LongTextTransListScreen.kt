package com.funny.translation.translate.ui.long_text

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.funny.compose.loading.DefaultEmpty
import com.funny.translation.debug.rememberStateOf
import com.funny.translation.helper.ClipBoardUtil
import com.funny.translation.helper.SimpleAction
import com.funny.translation.helper.readText
import com.funny.translation.helper.string
import com.funny.translation.helper.toastOnUi
import com.funny.translation.translate.LocalNavController
import com.funny.translation.translate.R
import com.funny.translation.translate.appCtx
import com.funny.translation.translate.database.LongTextTransTask
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.extentions.formatBraceStyle
import com.funny.translation.translate.ui.TranslateScreen
import com.funny.translation.translate.ui.widget.CommonPage
import com.funny.translation.translate.ui.widget.MultiFabItem
import com.funny.translation.translate.ui.widget.MultiFloatingActionButton
import com.funny.translation.translate.utils.DataHolder
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.floatingActionBarModifier
import okhttp3.internal.immutableListOf
import java.util.UUID

private val floatingActionItems = immutableListOf(
    MultiFabItem(id = 0, icon = Icons.Default.Edit, label = string(id = R.string.from_editable_text)),
    // 剪切板
    MultiFabItem(id = 1, icon = Icons.Default.ContentPaste, label = string(id = R.string.from_clipboard)),
    // 文件
    MultiFabItem(id = 2, icon = Icons.Default.FileOpen, label = string(id = R.string.from_file)),
)

@Composable
fun LongTextTransListScreen() {
    val navController = LocalNavController.current
    val navigateToDetailPage = remember {
        { id: String?, text: String? ->
            // 如果 id 为 null，则为创建任务并导航过去
            if (id == null) {
                val newId = UUID.randomUUID().toString()
                DataHolder.put(newId, text)
                navController.navigate(TranslateScreen.LongTextTransDetailScreen.route.formatBraceStyle("id" to newId))
            } else {
                navController.navigate(TranslateScreen.LongTextTransDetailScreen.route.formatBraceStyle("id" to id))
            }
        }
    }
    CommonPage(title = stringResource(id = R.string.long_text_trans)) {
        val list by appDB.longTextTransDao.getAll().collectAsState(initial = emptyList())

        LazyColumn {
            if (list.isNotEmpty()) {
                items(list, key = { it.id }) {
                    LongTextTransItem(it, onClick = {
                        navigateToDetailPage(it.id, null)
                    })
                }
            } else {
                item {
                    DefaultEmpty()
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

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) {
        it ?: return@rememberLauncherForActivityResult
        try {
            val text = it.readText(context)
            if (text.isNotBlank()) {
                navigateToDetailPage(null, text)
            } else {
                context.toastOnUi(string(id = R.string.file_empty))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            context.toastOnUi(string(id = R.string.file_read_error))
        }
    }
    
    MultiFloatingActionButton(
        modifier = Modifier.floatingActionBarModifier(),
        srcIcon = Icons.Default.Add,
        items = floatingActionItems,
        onFabItemClicked = { item ->
            when(item.id) {
                0 -> {
                    showInputDialog = true
                }
                1 -> {
                    // 从剪切板
                    val text = ClipBoardUtil.read(appCtx)
                    if (text.isBlank()) {
                        appCtx.toastOnUi(string(id = R.string.clipboard_empty))
                    } else {
                        navigateToDetailPage(null, text)
                    }
                }
                2 -> {
                    // 从文件，仅限于各类文本文件和 JSON 文件
                    filePickerLauncher.launch(arrayOf("text/*", "application/json"))
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LongTextTransItem(
    task: LongTextTransTask,
    onClick: SimpleAction
) {
    val oneLineText = @Composable { text: String ->
        Text(text = text, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            oneLineText(task.sourceText)
        },
        supportingContent = {
            oneLineText(task.resultText)
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