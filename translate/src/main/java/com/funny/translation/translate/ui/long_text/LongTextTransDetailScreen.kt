package com.funny.translation.translate.ui.long_text

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.jetsetting.core.ui.SimpleDialog
import com.funny.translation.debug.rememberStateOf
import com.funny.translation.helper.assetsStringLocalized
import com.funny.translation.helper.string
import com.funny.translation.helper.toastOnUi
import com.funny.translation.helper.writeText
import com.funny.translation.translate.LocalNavController
import com.funny.translation.translate.R
import com.funny.translation.translate.bean.AI_TEXT_POINT
import com.funny.translation.translate.ui.long_text.components.AIPointText
import com.funny.translation.translate.ui.long_text.components.RemarkDialog
import com.funny.translation.translate.ui.long_text.components.ResultTextPart
import com.funny.translation.translate.ui.long_text.components.SourceTextPart
import com.funny.translation.translate.ui.widget.CommonPage
import com.funny.translation.translate.ui.widget.TwoProgressIndicator
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.MarkdownText
import com.funny.translation.ui.floatingActionBarModifier
import java.util.UUID

@Composable
fun LongTextTransDetailScreen(
    id: String = UUID.randomUUID().toString(),
) {
    val vm: LongTextTransViewModel = viewModel()
    val navController = LocalNavController.current
    
    var showHelpDialog by rememberDataSaverState(key = "show_long_trans_tip", default = true) 
    if (showHelpDialog) {
        SimpleDialog(
            openDialog = showHelpDialog,
            updateOpenDialog = { showHelpDialog = it },
            content = {
                MarkdownText(markdown = assetsStringLocalized(name = "long_text_trans_help.md"))
            }
        )
    }

    // 跳转到其他页面时，暂停翻译
    DisposableEffect(key1 = Unit) {
        onDispose {
            if (vm.screenState == ScreenState.Translating && !vm.isPausing)
                vm.toggleIsPausing()
        }
    }
    
    CommonPage(
        title = stringResource(id = R.string.long_text_trans),
        actions = {
            AIPointText(planName = AI_TEXT_POINT)
            IconButton(onClick = { showHelpDialog = true }) {
                FixedSizeIcon(Icons.Default.Help, contentDescription = "Help")
            }
        },
    ) {
        // 传入参数时，先初始化各类型
        LaunchedEffect(key1 = id){
            vm.initArgs(id)
        }

        val quitAlertDialog = remember { mutableStateOf(false) }
        SimpleDialog(
            openDialogState = quitAlertDialog,
            title = stringResource(id = R.string.tip),
            message = stringResource(id = R.string.quit_translating_alert),
            confirmButtonAction = {
                navController.navigateUp()
            }
        )

        val remarkDialogState = rememberStateOf(value = false)
        RemarkDialog(
            showState = remarkDialogState,
            taskId = vm.transId,
            initialRemark = vm.task?.remark ?: "",
            updateRemarkAction = vm::updateRemark
        )

        BackHandler(enabled =
            (vm.screenState == ScreenState.Translating && !vm.isPausing)
            || (vm.screenState == ScreenState.Result && vm.task?.remark?.isEmpty() == true)
        ) {
            if (vm.screenState == ScreenState.Translating) {
                quitAlertDialog.value = true
            } else {
                remarkDialogState.value = true
            }
        }

//        NoticeBar(
//            modifier = Modifier
//                .fillMaxWidth(0.95f)
//                .background(
//                    MaterialTheme.colorScheme.primaryContainer,
//                    RoundedCornerShape(8.dp)
//                )
//                .padding(8.dp),
//            text = stringResource(R.string.early_preview_tip),
//            singleLine = false,
//            showClose = false,
//        )
//        Spacer(modifier = Modifier.height(4.dp))
        AnimatedVisibility (vm.screenState == ScreenState.Translating) {
            TwoProgressIndicator(startedProgress = vm.startedProgress, finishedProgress = vm.progress)
        }

        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)) {
            DetailContent(screenState = vm.screenState, vm = vm)
        }
    }

    AnimatedVisibility(
        visible = vm.screenState == ScreenState.Translating,
        enter = fadeIn() + expandIn(expandFrom = Alignment.Center),
        exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.Center)
    ) {
        FloatingActionButton(
            modifier = Modifier.floatingActionBarModifier(),
            onClick = vm::toggleIsPausing
        ) {
            AnimatedContent(targetState = vm.isPausing, label = "TogglePause") { pausing ->
                if (pausing) {
                    FixedSizeIcon(Icons.Default.PlayArrow, contentDescription = "Click To Play")
                } else {
                    FixedSizeIcon(Icons.Default.Pause, contentDescription = "Click To Pause")
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.DetailContent(
    screenState: ScreenState,
    vm: LongTextTransViewModel
) {
    AnimatedContent(targetState = screenState, label = "DetailContent") {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (it) {
                ScreenState.Init -> {
                    SourceTextPart(
                        text = vm.sourceText,
                        updateSourceText = vm::updateSourceText,
                        screenState = vm.screenState,
                        tokenCounter = vm.chatBot.tokenCounter
                    )
                    PromptPart(vm.prompt, vm.chatBot.tokenCounter, vm::updatePrompt)
                    Category(
                        title = stringResource(id = R.string.all_corpus),
                        helpText = string(R.string.corpus_help),
                    ) { expanded ->
                        AllCorpusList(vm = vm, expanded = expanded)
                    }
                    ModelListPart(modelList = vm.modelList, vm.selectedModelId, onModelSelected = vm::updateBot)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { vm.startTranslate() }) {
                        FixedSizeIcon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = stringResource(id = R.string.start_translate))
                    }
                }
                ScreenState.Translating -> {
                    SourceTextPart(
                        text = vm.sourceText,
                        screenState = vm.screenState,
                        currentTransStartOffset = vm.translatedLength,
                        currentTransLength = vm.currentTransPartLength,
                        tokenCounter = vm.chatBot.tokenCounter
                    )
                    ResultTextPart(
                        text = vm.resultText,
                        screenState = vm.screenState,
                        currentResultStartOffset = vm.currentResultStartOffset,
                        tokenCounter = vm.chatBot.tokenCounter
                    )
                    CorpusListPart(vm = vm)
                }
                ScreenState.Result -> {
                    SourceTextPart(text = vm.sourceText, screenState = vm.screenState, tokenCounter = vm.chatBot.tokenCounter)
                    ResultTextPart(text = vm.resultText, screenState = vm.screenState, tokenCounter = vm.chatBot.tokenCounter)
                    Spacer(modifier = Modifier.height(8.dp))
                    ExportButton(
                        exportOnlyResultProvider = vm::resultText,
                        exportBothSourceAndResultProvider = vm::generateBothExportedText
                    )
                }
            }
        }
    }
}


/**
 * Provider： 返回生成结果的函数
 * @param exportOnlyResultProvider Function0<String>
 * @param exportBothSourceAndResultProvider Function0<Pair<String, String>>
 */
@Composable
private fun ExportButton(
    exportOnlyResultProvider: () -> String,
    exportBothSourceAndResultProvider: () -> String,
) {
    var needToExportTextProvider by rememberStateOf(value = { "" })
    val context = LocalContext.current
    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("plain/text")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val text = needToExportTextProvider.invoke()
        if (text.isBlank()) {
            context.toastOnUi(string(id = R.string.export_text_empty))
            return@rememberLauncherForActivityResult
        }
        val watermark = string(id = R.string.export_watermark)
        context.toastOnUi(string(id = R.string.exporting))
        uri.writeText(context, "$text\n\n${"-".repeat(20)}\n$watermark")
        context.toastOnUi(string(id = R.string.export_success))
    }
    var expand by rememberStateOf(value = false)
    Button(onClick = { expand = true }) {
        FixedSizeIcon(Icons.Default.SaveAlt, contentDescription = null)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = stringResource(id = R.string.export_result))
        DropdownMenu(expanded = expand, onDismissRequest = { expand = false }) {
            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.export_only_result)) },
                onClick = {
                    needToExportTextProvider = exportOnlyResultProvider
                    exportFileLauncher.launch("result_${System.currentTimeMillis()}.txt")
                }
            )
            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.export_both_source_and_result)) },
                onClick = {
                    needToExportTextProvider = exportBothSourceAndResultProvider
                    exportFileLauncher.launch("source_and_result_${System.currentTimeMillis()}.txt")
                }
            )
        }
    }

}


@Composable
private fun TranslateButton(
    progress: Float = 1f,
    isTranslating: Boolean = false,
    onClick: () -> Unit
) {
    val borderColor = MaterialTheme.colorScheme.secondary
    val density = LocalDensity.current
    val size48dp = remember { with(density) { 48.dp.toPx() } }
    val size12dp = remember { with(density) { 12.dp.toPx() } }

    IconButton(
        modifier =
        Modifier.drawBehind {
            if (progress < 1f) drawArc(
                borderColor,
                startAngle = -90f,
                360f * progress,
                false,
                style = Stroke(width = 4f),
                topLeft = Offset(size12dp / 2, size12dp / 2),
                size = size.copy(size48dp - size12dp, size48dp - size12dp)
            )
        }, onClick = onClick
    ) {
        if (!isTranslating) FixedSizeIcon(
            Icons.Default.Done,
            contentDescription = stringResource(R.string.start_translate),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        else FixedSizeIcon(
            painter = painterResource(id = R.drawable.ic_pause),
            contentDescription = stringResource(R.string.stop_translate),
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }

}
