package com.funny.translation.translate.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.funny.translation.AppConfig
import com.funny.translation.Consts
import com.funny.translation.helper.toastOnUi
import com.funny.translation.translate.*
import com.funny.translation.translate.R
import com.funny.translation.translate.ui.widget.InputText
import com.funny.translation.ui.SubcomposeBottomFirstLayout
import kotlinx.coroutines.delay

@Composable
fun MainPartInputting(
    vm: MainViewModel,
    showSnackbar: (String) -> Unit,
    showEngineSelectAction: SimpleAction
) {
    val context = LocalContext.current
    fun startTranslate() {
        vm.updateMainScreenState(MainScreenState.Translating)
        val selectedEngines = vm.selectedEngines
        if (selectedEngines.isEmpty()) {
            showSnackbar(FunnyApplication.resources.getString(R.string.snack_no_engine_selected))
            return
        }
        val selectedSize = selectedEngines.size
        if (selectedSize > Consts.MAX_SELECT_ENGINES) {
            val resId = if (AppConfig.isVip()) R.string.message_out_of_max_engine_limit
            else R.string.message_out_of_max_engine_limit_novip
            showSnackbar(
                appCtx.getString(resId).format(Consts.MAX_SELECT_ENGINES, selectedSize)
            )
            return
        }
        if (!vm.isTranslating()) {
            vm.translate()
//            shouldRequestFocus = false
        } else {
            vm.cancel()
            context.toastOnUi(FunnyApplication.resources.getString(R.string.message_stop_translate))
        }
    }
    val goBackAction = remember {
        {
            vm.updateMainScreenState(MainScreenState.Normal)
        }
    }
    BackHandler(onBack = goBackAction)
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        UpperPartBackground {
            MainTopBarInputting(
                showEngineSelectAction = showEngineSelectAction,
                navigateBackAction = goBackAction
            )
            InputPart(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                vm = vm,
                updateShowListType = {},
                startTranslateActon = ::startTranslate
            )
        }
        LanguageSelectRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            sourceLanguage = vm.sourceLanguage,
            updateSourceLanguage = vm::updateSourceLanguage,
            targetLanguage = vm.targetLanguage,
            updateTargetLanguage = vm::updateTargetLanguage,
        )
    }
}

@Composable
private fun TranslateAndClearRow(
    vm: MainViewModel,
    startTranslateActon: SimpleAction
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, bottom = 6.dp, top = 4.dp),
        horizontalArrangement = Arrangement.End
    ) {
        ElevatedButton(
            onClick = startTranslateActon,
            modifier = Modifier
        ) {
            Text(
                text = stringResource(id = R.string.translate),
            )
        }
        ElevatedButton(
            onClick = { vm.translateText = "" },
            modifier = Modifier
                .padding(start = 8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.clear_content),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopBarInputting(
    navigateBackAction: SimpleAction,
    showEngineSelectAction: SimpleAction
) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = navigateBackAction) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = stringResource(id = R.string.back)
                )
            }
        },
        actions = {
            IconButton(onClick = showEngineSelectAction) {
                Icon(
                    painterResource(id = R.drawable.ic_translate),
                    contentDescription = stringResource(id = R.string.engine_select),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun InputPart(
    modifier: Modifier,
    vm: MainViewModel,
    updateShowListType: (ShowListType) -> Unit,
    startTranslateActon: SimpleAction
) {
    var shouldRequestFocus by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        // 等待绘制完成后再请求焦点
        delay(100)
        shouldRequestFocus = true
    }

    SubcomposeBottomFirstLayout(
        modifier,
        bottom = {
            val rowVisible by remember { derivedStateOf { vm.actualTransText.isNotEmpty() }}
            AnimatedVisibility(
                visible = rowVisible,
                enter = slideInVertically { fullHeight -> fullHeight } + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { fullHeight -> fullHeight }) + fadeOut()
            ) {
                TranslateAndClearRow(vm, startTranslateActon = startTranslateActon)
            }
        }
    ) {
        InputText(
            modifier = Modifier
                .fillMaxSize(),
            textProvider = { vm.translateText },
            updateText = {
                vm.updateTranslateText(it)
                if (it == "") updateShowListType(ShowListType.History)
            },
            shouldRequest = shouldRequestFocus,
            updateFocusRequest = {
                if (it != shouldRequestFocus) shouldRequestFocus = it
            },
            translateAction = startTranslateActon
        )
    }
}

// 这个 Composable 会在圆形按钮上绘制进度条
// 现在被弃用，可以下载 v2.6.1 版本的 apk 来看效果
@Composable
private fun TranslateButton(
    progress: Int = 100,
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
            if (progress < 100) drawArc(
                borderColor,
                startAngle = -90f,
                360f * progress / 100,
                false,
                style = Stroke(width = 4f),
                topLeft = Offset(size12dp / 2, size12dp / 2),
                size = size.copy(size48dp - size12dp, size48dp - size12dp)
            )
        }, onClick = onClick
    ) {
        if (!isTranslating) Icon(
            Icons.Default.Done,
            contentDescription = "开始翻译",
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        else Icon(
            painter = painterResource(id = R.drawable.ic_pause),
            contentDescription = "停止翻译",
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }

}
