package com.funny.translation.translate.ui.main

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.funny.cmaterialcolors.MaterialColors
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.trans.*
import com.funny.translation.translate.*
import com.funny.translation.translate.R
import com.funny.translation.translate.bean.Consts
import com.funny.translation.translate.ui.bean.RoundCornerConfig
import com.funny.translation.translate.ui.widget.*
import com.funny.translation.translate.utils.AudioPlayer
import com.funny.translation.translate.utils.ClipBoardUtil
import com.google.accompanist.flowlayout.FlowRow
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private const val TAG = "MainScreen"

private interface UpdateSelectedEngine {
    fun add(engine: TranslationEngine)
    fun remove(engine: TranslationEngine)
}

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun MainScreen() {
    val vm: MainViewModel = viewModel()

    val updateSelectedEngine = object : UpdateSelectedEngine {
        override fun add(engine: TranslationEngine) {
            vm.addSelectedEngines(engine)
        }

        override fun remove(engine: TranslationEngine) {
            vm.removeSelectedEngine(engine)
        }
    }

    val bindEngines by vm.bindEnginesFlow.collectAsState(emptyList())
    val jsEngines by vm.jsEnginesFlow.collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarState.current

    val showSnackbar: (String) -> Unit = {
        scope.launch {
            snackbarHostState.showSnackbar(it)
        }
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val activityVM: ActivityViewModel = LocalActivityVM.current
    val softwareKeyboardController = LocalSoftwareKeyboardController.current

//    SideEffect {
//        Log.d(TAG, "MainScreen: text:$translateText sourceId:$source targetId:$target")
//    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth > 720.dp) { // 横屏
            val scrollState = rememberScrollState()
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                EngineSelect(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.3f)
                        .padding(8.dp)
                        .verticalScroll(scrollState),
                    bindEngines, jsEngines,
                    updateSelectedEngine
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.95f)
                        .width(2.dp)
                        .background(MaterialTheme.colors.surface)
                )
                Column(
                    Modifier
                        .fillMaxHeight()
                        .padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TranslatePart(
                        vm = vm,
                        showSnackbar = showSnackbar,
                        modifier = Modifier
                    )
                }
            }
        } else {
            var expandEngineSelect by remember { mutableStateOf(false) }
            var engineSelectHeight by remember { mutableStateOf(0) }
            val swipeOffset = remember { Animatable(0f) }

            val nestedScrollConnection = remember {
                object : NestedScrollConnection {
                    override fun onPostScroll(
                        consumed: Offset,
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset {
                        // 仅在展开时处理嵌套滑动（向上收回）
                        if (!expandEngineSelect) return super.onPostScroll(
                            consumed,
                            available,
                            source
                        )
//                        Log.d(TAG, "onPostScroll: source: $source available: $available offset: ${swipeOffset.value}")
                        if (source == NestedScrollSource.Drag) { // 如果在拖动
                            if (swipeOffset.value > 0.2 * engineSelectHeight) {
                                expandEngineSelect = false
                                scope.launch { swipeOffset.snapTo(0f) }
                                return Offset(0f, available.y)
                            }
                            // 如果向上拖并且展开了
                            if (available.y < 0 && expandEngineSelect) {
                                // -负数等于加上正数
                                scope.launch { swipeOffset.snapTo(swipeOffset.value - available.y) }
                                return Offset(0f, available.y)
                            }
                        } else if (source == NestedScrollSource.Fling) { // 手已经松了，但还开着
                            if (abs(available.y) < 1f && swipeOffset.value > 0f && !swipeOffset.isRunning) {
                                Log.d(TAG, "onPostScroll: 手松了但还展开着，手动关闭")
                                scope.launch { swipeOffset.animateTo(0f) }
                                return Offset(0f, available.y)
                            }
                        }
                        return super.onPostScroll(consumed, available, source)
                    }
                }
            }
            Column(
                modifier = Modifier
                    .offset { IntOffset(0, -swipeOffset.value.roundToInt()) }
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .fillMaxWidth()
                    .nestedScroll(nestedScrollConnection),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(visible = expandEngineSelect) {
                    EngineSelect(
                        modifier = Modifier
                            .onGloballyPositioned {
                                if (engineSelectHeight == 0) engineSelectHeight = it.size.height
//                                Log.d(TAG, "MainScreen: EngineSelect Height :$engineSelectHeight")
                            }
                            .padding(8.dp),
                        bindEngines, jsEngines,
                        updateSelectedEngine
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier
                    .clip(CircleShape)
                    .fillMaxWidth(0.4f)
                    .height(12.dp)
                    .background(MaterialTheme.colors.secondary)
                    .clickable {
                        expandEngineSelect = !expandEngineSelect
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                TranslatePart(
                    vm = vm,
                    showSnackbar = showSnackbar,
                    modifier = Modifier.fillMaxWidth()
                )
                var singleLine by remember {
                    mutableStateOf(true)
                }
                val notice by activityVM.noticeInfo
                notice?.let {
                    NoticeBar(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentHeight(Alignment.Bottom)
                            .clickable {
                                if (it.url.isNullOrEmpty()) singleLine = !singleLine
                                else WebViewActivity.start(context, it.url)
                            }
                            .background(MaterialTheme.colors.surface, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                            .animateContentSize(),
                        text = it.message,
                        singleLine = singleLine,
                        showClose = true,
                    )
                }

            }
        }
    }

    // 接收其他应用传来的参数并开始翻译
//    val translateTextState = remember {
//        mutableStateOf(activityVM.tempTransConfig.sourceString)
//    }

    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
//            Log.d(TAG, "MainScreen: event: $event")
            if (event == Lifecycle.Event.ON_RESUME) {
//                Log.d(TAG, "MainScreen: activityVM:${activityVM.hashCode()}")
                val text = activityVM.tempTransConfig.sourceString?.trim() ?: ""
//                Log.d(TAG, "MainScreen: get_text:$text")
                if (text != "") {
                    vm.translateText.value = text
                    if (activityVM.tempTransConfig.sourceLanguage != null) {
                        vm.sourceLanguage.value = activityVM.tempTransConfig.sourceLanguage
                    }
                    if (activityVM.tempTransConfig.targetLanguage != null) {
                        vm.targetLanguage.value = activityVM.tempTransConfig.targetLanguage
                    }
                    vm.translate()
                    activityVM.tempTransConfig.clear()
                }
            } else if (event == Lifecycle.Event.ON_STOP) {
                softwareKeyboardController?.hide()
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TranslatePart(
    vm: MainViewModel,
    showSnackbar: (String) -> Unit,
    modifier: Modifier
) {
    val transText by vm.translateText.observeAsState("")
    val sourceLanguage by vm.sourceLanguage.observeAsState()
    val targetLanguage by vm.targetLanguage.observeAsState()

    val resultList by vm.resultList.observeAsState()
    val translateProgress by vm.progress.observeAsState()
    val animateProgress = remember {
        Animatable(0f)
    }
    LaunchedEffect(translateProgress){
        animateProgress.animateTo(translateProgress!!)
    }

    val softKeyboardController = LocalSoftwareKeyboardController.current
    Row( // 语种选择
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        LanguageSelect(
            language = sourceLanguage!!,
            languages = allLanguages,
            updateLanguage = {
                vm.sourceLanguage.value = it
                DataSaverUtils.saveData(Consts.KEY_SOURCE_LANGUAGE, it.id)
            }
        )
        ExchangeButton {
            Log.d(TAG, "MainScreen: clicked")
            val temp = sourceLanguage
            vm.sourceLanguage.value = targetLanguage
            vm.targetLanguage.value = temp

            DataSaverUtils.saveData(Consts.KEY_SOURCE_LANGUAGE, vm.sourceLanguage.value!!.id)
            DataSaverUtils.saveData(Consts.KEY_TARGET_LANGUAGE, vm.targetLanguage.value!!.id)
        }
        LanguageSelect(
            language = targetLanguage!!,
            languages = allLanguages,
            updateLanguage = {
                vm.targetLanguage.value = it
                DataSaverUtils.saveData(Consts.KEY_TARGET_LANGUAGE, it.id)
            }
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
    InputText(text = transText, updateText = { vm.translateText.value = it }) // 输入框
    Spacer(modifier = Modifier.height(12.dp))
    TranslateButton(animateProgress.value.toInt()) { // 翻译按钮
        val selectedEngines = vm.selectedEngines
        if (selectedEngines.isEmpty()) {
            showSnackbar(FunnyApplication.resources.getString(R.string.snack_no_engine_selected))
            return@TranslateButton
        }
        val selectedSize = selectedEngines.size
        if (selectedSize > Consts.MAX_SELECT_ENGINES) {
            showSnackbar(
                FunnyApplication.resources.getString(R.string.message_out_of_max_engine_limit)
                    .format(Consts.MAX_SELECT_ENGINES, selectedSize)
            )
            return@TranslateButton
        }
        if (!vm.isTranslating()) {
            vm.translate()
            softKeyboardController?.hide()
        } else {
            vm.cancel()
            showSnackbar(FunnyApplication.resources.getString(R.string.message_stop_translate))
        }
    }
    Spacer(modifier = Modifier.height(18.dp))
    TranslationList(resultList!!, showSnackbar) // 结果列表
}

@ExperimentalAnimationApi
@Composable
private fun EngineSelect(
    modifier: Modifier,
    bindEngines: List<TranslationEngine> = arrayListOf(),
    jsEngines: List<TranslationEngine> = arrayListOf(),
    updateSelectEngine: UpdateSelectedEngine
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = stringResource(id = R.string.bind_engine),
            fontWeight = W600
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            mainAxisSpacing = 8.dp,
            crossAxisSpacing = 0.dp
        ) {
            bindEngines.forEachIndexed { index, task ->
                SelectableChip(initialSelect = task.selected, text = task.name) {
                    if (!task.selected) { // 选中了
                        updateSelectEngine.add(task)
                    } else updateSelectEngine.remove(task)
                    bindEngines[index].selected = !task.selected
                    DataSaverUtils.saveData(task.selectKey, task.selected)
                }
            }
        }

        if (jsEngines.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.plugin_engine),
                fontWeight = W600
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                mainAxisSpacing = 8.dp,
                crossAxisSpacing = 0.dp
            ) {
                jsEngines.forEachIndexed { index, task ->
                    //临时出来的解决措施，因为ArrayList单个值更新不会触发LiveData的更新。更新自己
                    SelectableChip(initialSelect = task.selected, text = task.name) {
                        if (!task.selected) { // 选中了
                            updateSelectEngine.add(task)
                        } else updateSelectEngine.remove(task)

                        jsEngines[index].selected = !task.selected
                        DataSaverUtils.saveData(task.selectKey, task.selected)
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageSelect(
    language: Language,
    languages: List<Language>,
    updateLanguage: (Language) -> Unit,
) {
    var expanded by remember {
        mutableStateOf(false)
    }
    RoundCornerButton(text = language.displayText, onClick = {
        expanded = true
    }) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            languages.forEach {
                DropdownMenuItem(onClick = {
                    updateLanguage(it)
                    expanded = false
                }) {
                    Text(it.displayText)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TranslationList(
    resultList: List<TranslationResult>,
    showSnackbar: (String) -> Unit
) {
    val size = resultList.size
    LazyColumn(
        modifier = Modifier,
        verticalArrangement = spacedBy(4.dp)
    ) {
        itemsIndexed(resultList, key = { _, r -> r.engineName }) { index, result ->
//            Log.d(TAG, "TranslationList: $result")
            TranslationItem(
                modifier = Modifier.animateItemPlacement(),
                result = result, roundCornerConfig = when (index) {
                    0 -> if (size == 1) RoundCornerConfig.All else RoundCornerConfig.Top
                    size - 1 -> RoundCornerConfig.Bottom
                    else -> RoundCornerConfig.None
                }, showSnackbar = showSnackbar
            )
        }
    }
}

@Composable
fun TranslateButton(
    progress: Int = 100,
    onClick: () -> Unit
) {
    val bgColor = MaterialTheme.colors.primary
    val p = if (progress == 0) 100 else progress
    Box(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
            .padding(0.dp)
            .clip(CircleShape)
            .drawWithContent {
                drawRect(
                    MaterialColors.Grey200,
                    Offset.Zero,
                    size,
                )
                drawRoundRect(
                    bgColor,
                    Offset.Zero,
                    size.copy(width = size.width * p / 100f),
                    CornerRadius(size.height / 2)
                )
                drawContent()
            }
            .clickable(onClick = onClick),
    ) {
        Text(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center),
            text = stringResource(id = R.string.translate),
            color = Color.White,
            fontSize = 22.sp,
            textAlign = TextAlign.Center,
            fontWeight = W500,
            letterSpacing = 8.sp
        )
    }

}


@Composable
fun TranslationItem(
    modifier: Modifier,
    result: TranslationResult,
    roundCornerConfig: RoundCornerConfig,
    showSnackbar: (String) -> Unit
) {
    val cornerSize = 16.dp
    val shape = when (roundCornerConfig) {
        is RoundCornerConfig.Top -> RoundedCornerShape(topStart = cornerSize, topEnd = cornerSize)
        is RoundCornerConfig.Bottom -> RoundedCornerShape(
            bottomEnd = cornerSize,
            bottomStart = cornerSize
        )
        is RoundCornerConfig.All -> RoundedCornerShape(cornerSize)
        is RoundCornerConfig.None -> RectangleShape
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.surface, shape = shape)
            .padding(12.dp)
            .animateContentSize()

    ) {
        var expandDetail by remember {
            mutableStateOf(false)
        }
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = result.engineName, color = MaterialColors.Grey600, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            val fontSize = when (result.basicResult.trans.length) {
                in 0..25 -> 24
                in 26..50 -> 20
                in 50..70 -> 18
                in 70..90 -> 16
                else -> 14
            }
            Text(
                text = result.basicResult.trans,
                color = MaterialTheme.colors.onSurface,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = {
                        ClipBoardUtil.copy(FunnyApplication.ctx, result.basicResult.trans)
                        showSnackbar(FunnyApplication.resources.getString(R.string.snack_finish_copy))
                    }, modifier = Modifier
//                        .then(Modifier.size(36.dp))
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.secondary)
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_copy_content),
                        contentDescription = stringResource(id = R.string.copy_content),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        AudioPlayer.play(
                            result.basicResult.trans.trim(), result.targetLanguage!!
                        ) {
                            showSnackbar(FunnyApplication.resources.getString(R.string.snack_speak_error))
                        }
                    }, modifier = Modifier
//                        .then(Modifier.size(36.dp))
                        .clip(CircleShape)
                        .size(48.dp)
                        .background(MaterialTheme.colors.secondary)
                        .pointerInput(Unit) {
                            detectTapGestures(onLongPress = {
                                AudioPlayer.pause()
                            })
                        }
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_speak),
                        contentDescription = stringResource(id = R.string.speak),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                if (!result.detailText.isNullOrEmpty()) {
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                        ExpandMoreButton {
                            expandDetail = !expandDetail
                        }
                    }
                }
            }
            if (expandDetail) {
                SelectionContainer {
                    MarkdownText(
                        markdown = result.detailText!!,
                        Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}