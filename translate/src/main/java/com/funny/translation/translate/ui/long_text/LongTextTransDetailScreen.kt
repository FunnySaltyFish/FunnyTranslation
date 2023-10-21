package com.funny.translation.translate.ui.long_text

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.funny.translation.translate.R
import com.funny.translation.translate.ui.long_text.components.ResultTextPart
import com.funny.translation.translate.ui.long_text.components.SourceTextPart
import com.funny.translation.translate.ui.main.LanguageSelectRow
import com.funny.translation.translate.ui.widget.CommonPage
import com.funny.translation.translate.ui.widget.NoticeBar
import com.funny.translation.translate.ui.widget.TwoProgressIndicator
import com.funny.translation.ui.FixedSizeIcon
import java.util.UUID

@Composable
fun LongTextTransDetailScreen(
    id: String = UUID.randomUUID().toString(),
    sourceTextKey: String
) {
    val vm: LongTextTransViewModel = viewModel()
    CommonPage(
        title = stringResource(id = R.string.long_text_trans),
        actions = {
//            Row {
//                TranslateButton(progress = vm.progress, onClick = vm::startTranslate )
//            }
        }
    ) {
        // 传入参数时，先初始化各类型
        LaunchedEffect(key1 = id){
            vm.initArgs(id, sourceTextKey)
        }

        NoticeBar(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    RoundedCornerShape(8.dp)
                )
                .padding(8.dp),
            text = stringResource(R.string.early_preview_tip),
            singleLine = false,
            showClose = false,
        )
        Spacer(modifier = Modifier.height(4.dp))
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
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.BottomEnd)
                .offset(x = (-40).dp, y = (-40).dp),
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            when (it) {
                ScreenState.Init -> {
                    SourceTextPart(text = vm.sourceString, screenState = vm.screenState)
                    PromptPart(vm.prompt, vm::updatePrompt)
                    Category(title = stringResource(id = R.string.all_corpus)) {
                        AllCorpusList(vm = vm)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { vm.startTranslate() }) {
                        FixedSizeIcon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = stringResource(id = R.string.start_translate))
                    }
                }
                ScreenState.Translating -> {
                    SourceTextPart(
                        text = vm.sourceString,
                        screenState = vm.screenState,
                        currentTransStartOffset = vm.translatedLength,
                        currentTransLength = vm.currentTransPartLength
                    )

                    ResultTextPart(
                        text = vm.resultText,
                        screenState = vm.screenState,
                        currentResultStartOffset = vm.currentResultStartOffset
                    )
                    CorpusListPart(vm = vm)
                }
                ScreenState.Result -> {
                    ResultTextPart(text = vm.resultText, screenState = vm.screenState)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { /*TODO*/ }) {
                        FixedSizeIcon(Icons.Default.SaveAlt, contentDescription = "export")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = stringResource(id = R.string.export_result))
                    }
                }
            }
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

@Composable
private fun FunctionRow(modifier: Modifier, vm: LongTextTransViewModel) {
//    Row(modifier) {
        LanguageSelectRow(
            modifier = modifier,
            sourceLanguage = vm.sourceLanguage,
            updateSourceLanguage = vm::updateSourceLanguage,
            targetLanguage = vm.targetLanguage,
            updateTargetLanguage = vm::updateTargetLanguage
        )
//    }
}

