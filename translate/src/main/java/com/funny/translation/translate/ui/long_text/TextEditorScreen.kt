package com.funny.translation.translate.ui.long_text

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.navOptions
import com.funny.jetsetting.core.ui.SimpleDialog
import com.funny.translation.debug.rememberSaveableStateOf
import com.funny.translation.debug.rememberStateOf
import com.funny.translation.helper.DateUtils
import com.funny.translation.translate.LocalNavController
import com.funny.translation.translate.R
import com.funny.translation.translate.database.Draft
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.ui.TranslateScreen
import com.funny.translation.translate.ui.widget.CommonPage
import com.funny.translation.ui.FixedSizeIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TextEditorScreen(
    draftId: Int? = null,
    initialText: String = ""
) {
    val showDialog = rememberStateOf(value = false)
    val navController = LocalNavController.current
    var text by rememberSaveableStateOf(initialText)
    val textEmpty by remember { derivedStateOf { text == "" } }

    SimpleDialog(
        openDialogState = showDialog,
        title = stringResource(id = R.string.tip),
        message = if (draftId == null) stringResource(id = R.string.save_draft) else stringResource(id = R.string.update_draft),
        confirmButtonText = stringResource(id = R.string.save),
        confirmButtonAction = {
            // 保存或者更新草稿
            CoroutineScope(Dispatchers.IO).launch {
                appDB.draftDao.upsert(Draft(content = text, remark = DateUtils.getNowStr(), id = draftId ?: 0))
                withContext(Dispatchers.Main) {
                    navController.popBackStack()
                }
            }
        },
        dismissButtonText = stringResource(id = R.string.exit),
        dismissButtonAction = {
            navController.popBackStack()
        }
    )

    BackHandler(!textEmpty) {
        if (text == "" || text == initialText) {
            navController.popBackStack()
        } else {
            showDialog.value = true
        }
    }
    CommonPage(
        actions = {
            AnimatedVisibility(visible = !textEmpty) {
                IconButton(onClick = {
                    navigateToLongTextTransDetailPage(
                        navController = navController,
                        id = null,
                        text = text,
                        navOptions = navOptions {
                            popUpTo(TranslateScreen.LongTextTransScreen.route)
                        })
                }) {
                    FixedSizeIcon(Icons.Default.PlayArrow, contentDescription = "Start")
                }
            }
        }
    ) {
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(key1 = focusRequester) {
            delay(500)
            focusRequester.requestFocus()
        }
        val textStyle = MaterialTheme.typography.bodyLarge
        Box(modifier = Modifier.imeNestedScroll()) {
            BasicTextField(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .imePadding()
                    .focusable(true)
                    .focusRequester(focusRequester),
                value = text,
                onValueChange = { text = it },
                textStyle = textStyle,
            ) {
                if (textEmpty) {
                    Text(
                        text = stringResource(id = R.string.input_text_hint),
                        style = textStyle,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
                it()
            }
        }
    }
}