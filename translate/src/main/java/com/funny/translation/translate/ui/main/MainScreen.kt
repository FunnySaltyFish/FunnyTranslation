@file:OptIn(ExperimentalMaterial3Api::class)

package com.funny.translation.translate.ui.main

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.funny.cmaterialcolors.MaterialColors
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.translation.Consts
import com.funny.translation.helper.ClipBoardUtil
import com.funny.translation.translate.*
import com.funny.translation.translate.R
import com.funny.translation.translate.database.TransHistoryBean
import com.funny.translation.translate.engine.selectKey
import com.funny.translation.translate.ui.bean.RoundCornerConfig
import com.funny.translation.translate.ui.widget.*
import com.funny.translation.translate.utils.AudioPlayer
import com.funny.translation.ui.touchToScale
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val TAG = "MainScreen"
internal typealias SimpleAction = () -> Unit

// 用于选择引擎时的回调
private interface UpdateSelectedEngine {
    fun add(engine: TranslationEngine)
    fun remove(engine: TranslationEngine)
}

enum class ShowListType {
    History, Result
}

// 当前主页面正处在什么状态
enum class MainScreenState {
    Normal,     // 正常情况
    Inputting,  // 正在输入
    Translating // 正在翻译
}

/**
 * 项目的翻译页面, [图片](https://web.funnysaltyfish.fun/temp_img/202111102032441.jpg)
 */
@OptIn(
    ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun MainScreen() {
    TextTransScreen()
}

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun TextTransScreen() {
    val vm: MainViewModel = viewModel()
    val context = LocalContext.current

    // 内置引擎
    val bindEngines by vm.bindEnginesFlow.collectAsState(emptyList())
    // 插件
    val jsEngines by vm.jsEnginesFlow.collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    // 使用 staticCompositionLocal 传递主页面 scaffold 的 snackbarHostState
    // 方便各个页面展示 snackBar
    // CompositionLocal 相关知识可参阅 https://developer.android.google.cn/jetpack/compose/compositionlocal?hl=zh-cn
    val snackbarHostState = LocalSnackbarState.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val activityVM: ActivityViewModel = LocalActivityVM.current
    val softwareKeyboardController = LocalSoftwareKeyboardController.current

    // Compose函数会被反复重新调用（重组），所以变量要remember
    val updateSelectedEngine = remember {
        object : UpdateSelectedEngine {
            override fun add(engine: TranslationEngine) {
                vm.addSelectedEngines(engine)
            }

            override fun remove(engine: TranslationEngine) {
                vm.removeSelectedEngine(engine)
            }
        }
    }

    val showSnackbar: (String) -> Unit = remember {
        {
            scope.launch {
                snackbarHostState.showSnackbar(it, withDismissAction = true)
            }
        }
    }

    val updateShowListType: (type: ShowListType) -> Unit = remember {
        {
            vm.showListType = it
        }
    }

    var showEngineSelect by remember { mutableStateOf(false) }
    if (showEngineSelect) {
        AlertDialog(onDismissRequest = { showEngineSelect = false }, text = {
            EngineSelect(
                modifier = Modifier,
                bindEngines,
                jsEngines,
                updateSelectedEngine
            )
        }, confirmButton = {
            TextButton(onClick = { showEngineSelect = false }) {
                Text(text = stringResource(id = R.string.confirm))
            }
        })
    }

    val showEngineSelectAction = remember {
        {
            showEngineSelect = true
        }
    }

    // 使用 BoxWithConstraints 用于适配横竖屏
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth > 720.dp) { // 横屏
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.95f)
                        .width(0.16f.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                )
                MainPart(showEngineSelectAction = showEngineSelectAction, showSnackbar = showSnackbar)
            }
        } else {
            MainPart(showEngineSelectAction = showEngineSelectAction, showSnackbar = showSnackbar)
        }
    }


    // DisposableEffect 是副作用的一种，相较于其他几个 SideEffect，特点在于可取消
    // 有关更多副作用，可参阅 https://developer.android.google.cn/jetpack/compose/side-effects?hl=zh-cn
    // 此处用于观察生命周期
    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val text = activityVM.tempTransConfig.sourceString?.trim() ?: ""
                if (text != "") {
                    vm.translateText = text
                    if (activityVM.tempTransConfig.sourceLanguage != null) {
                        vm.sourceLanguage = activityVM.tempTransConfig.sourceLanguage!!
                    }
                    if (activityVM.tempTransConfig.targetLanguage != null) {
                        vm.targetLanguage = activityVM.tempTransConfig.targetLanguage!!
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

@Composable
private fun MainPart(
    modifier: Modifier = Modifier,
    showEngineSelectAction: SimpleAction,
    showSnackbar: (String) -> Unit,
) {
    val vm: MainViewModel = viewModel()
    SimpleNavigation(currentScreen = vm.currentState, modifier = modifier) { state ->
        when (state) {
            MainScreenState.Normal -> MainPartNormal(
                vm = vm,
                showEngineSelectAction = showEngineSelectAction
            )
            MainScreenState.Inputting -> MainPartInputting(
                vm = vm,
                showEngineSelectAction = showEngineSelectAction,
                showSnackbar = showSnackbar
            )
            MainScreenState.Translating -> MainPartTranslating(vm = vm)
        }
    }
}


@Composable
fun ColumnScope.UpperPartBackground(
    content: @Composable ColumnScope.() -> Unit
) {
    val color = MaterialTheme.colorScheme.surface
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .background(color, shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)),
        content = content,
        horizontalAlignment = Alignment.CenterHorizontally
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
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
            fontWeight = FontWeight.W600
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            bindEngines.forEach { task ->
                var taskSelected by rememberDataSaverState(
                    key = task.selectKey,
                    default = task.selected
                )
                FilterChip(selected = taskSelected, onClick = {
                    if (!taskSelected) { // 选中了
                        updateSelectEngine.add(task)
                    } else updateSelectEngine.remove(task)
                    taskSelected = !taskSelected
                }, label = {
                    Text(text = task.name)
                })
            }
        }

        if (jsEngines.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.plugin_engine),
                fontWeight = FontWeight.W600
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                jsEngines.forEach { task ->
                    var taskSelected by rememberDataSaverState(
                        key = task.selectKey,
                        default = task.selected
                    )
                    FilterChip(selected = taskSelected, onClick = {
                        if (!taskSelected) { // 选中了
                            updateSelectEngine.add(task)
                        } else updateSelectEngine.remove(task)
                        taskSelected = !taskSelected
                    }, label = {
                        Text(text = task.name)
                    })
                }
            }
        }
    }
}


@Composable
private fun ResultPart(vm: MainViewModel, showSnackbar: (String) -> Unit) {
    val showHistory by rememberDataSaverState(key = Consts.KEY_SHOW_HISTORY, default = false)
    if (showHistory && vm.showListType == ShowListType.History) {
        TransHistoryList(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            transHistories = vm.transHistories.collectAsLazyPagingItems(),
            onClickHistory = { transHistory ->
                vm.translateText = transHistory.sourceString
                vm.sourceLanguage = findLanguageById(transHistory.sourceLanguageId)
                vm.targetLanguage = findLanguageById(transHistory.targetLanguageId)
                vm.translate()
            },
            onDeleteHistory = { sourceString ->
                vm.deleteTransHistory(sourceString)
            }
        )
    } else {
        TranslationList(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            resultList = vm.resultList,
            showSnackbar = showSnackbar,
            sourceLanguage = vm.sourceLanguage,
            sourceText = vm.actualTransText
        )
    }
}

@Composable
fun MainTopBar(
    state: MainScreenState,
    showDrawerAction: (() -> Unit)? = null,
) {
    when(state){
        MainScreenState.Normal -> MainTopBarNormal(showDrawerAction = showDrawerAction)
        else -> {}
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TransHistoryList(
    modifier: Modifier,
    transHistories: LazyPagingItems<TransHistoryBean>,
    onClickHistory: (TransHistoryBean) -> Unit,
    onDeleteHistory: (String) -> Unit,
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = modifier.background(
            MaterialTheme.colorScheme.primaryContainer,
            RoundedCornerShape(4.dp)
        ), reverseLayout = true // 这一条使得最新的历史会在最下面
    ) {
        items(transHistories) { transHistory ->
            transHistory ?: return@items
            Row(
                Modifier
                    .fillMaxWidth()
                    .touchToScale {
                        onClickHistory(transHistory)
                    }
                    .padding(start = 8.dp)
                    .animateItemPlacement(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = transHistory.sourceString,
                    fontWeight = W600,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 16.sp
                )
                Row {
                    IconButton(onClick = {
                        ClipBoardUtil.copy(
                            context,
                            transHistory.sourceString
                        )
                    }) {
                        Icon(
                            painterResource(id = R.drawable.ic_copy_content),
                            stringResource(R.string.copy)
                        )
                    }
                    IconButton(onClick = {
                        onDeleteHistory(transHistory.sourceString)
                    }) {
                        Icon(Icons.Default.Delete, "删除此历史记录")
                    }
                }
            }
        }
    }
}



@Composable
private fun LanguageSelect(
    modifier: Modifier = Modifier,
    language: Language,
    languages: List<Language>,
    updateLanguage: (Language) -> Unit,
) {
    var expanded by remember {
        mutableStateOf(false)
    }
    Button(
        modifier = modifier, onClick = {
            expanded = true
        }, shape = RoundedCornerShape(8.dp), colors = buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ), contentPadding = PaddingValues(horizontal = 40.dp, vertical = 16.dp)
    ) {
        Text(text = language.displayText, fontSize = 18.sp, fontWeight = W600)
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            languages.forEach {
                DropdownMenuItem(onClick = {
                    updateLanguage(it)
                    expanded = false
                }, text = {
                    Text(it.displayText)
                })
            }
        }
    }
}

@Composable
private fun TranslationList(
    modifier: Modifier,
    sourceLanguage: Language,
    sourceText: String,
    resultList: List<TranslationResult>,
    showSnackbar: (String) -> Unit,
) {
    val size = resultList.size
    LazyColumn(
        modifier = modifier,
        verticalArrangement = spacedBy(4.dp),
    ) {
        item {
            Row(
                Modifier
                    .touchToScale()
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(16.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.speak_source_string),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                // 朗读原文
                SpeakButton(
                    text = sourceText,
                    language = sourceLanguage,
                    showSnackbar = showSnackbar
                )
            }
        }
        itemsIndexed(resultList, key = { _, r -> r.engineName }) { index, result ->
//            Log.d(TAG, "TranslationList: $result")
            ResultItem(
                modifier = Modifier.fillMaxWidth(),
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
private fun ResultItem(
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
    val offsetAnim = remember { Animatable(100f) }
    LaunchedEffect(Unit) {
        offsetAnim.animateTo(0f)
    }
    Box(
        modifier = modifier
            .touchToScale()
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.primaryContainer, shape = shape)
            .padding(12.dp)
            .offset { IntOffset(offsetAnim.value.roundToInt(), 0) }
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
            SelectionContainer {
                Text(
                    text = result.basicResult.trans,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = {
                        ClipBoardUtil.copy(FunnyApplication.ctx, result.basicResult.trans)
                        showSnackbar(FunnyApplication.resources.getString(R.string.snack_finish_copy))
                    }, modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_copy_content),
                        contentDescription = stringResource(id = R.string.copy_content),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                SpeakButton(
                    text = result.basicResult.trans.trim(),
                    language = result.targetLanguage!!,
                    showSnackbar = showSnackbar
                )
                if (!result.detailText.isNullOrEmpty()) {
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                        ExpandMoreButton(expand = expandDetail) {
                            expandDetail = it
                        }
                    }
                }
            }
            if (expandDetail) {
                MarkdownText(
                    markdown = result.detailText!!,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                )
            }
        }
    }
}

@Composable
private fun SpeakButton(
    text: String,
    language: Language,
    showSnackbar: (String) -> Unit
) {
    val speakerState = rememberFrameAnimIconState(
        listOf(R.drawable.ic_speaker_2, R.drawable.ic_speaker_1),
    )
    LaunchedEffect(AudioPlayer.currentPlayingText) {
        // 修正：当列表划出屏幕后state与实际播放不匹配的情况
        if (AudioPlayer.currentPlayingText != text && speakerState.isPlaying) {
            speakerState.reset()
        }
    }
    IconButton(
        onClick = {
            if (text == AudioPlayer.currentPlayingText) {
                speakerState.reset()
                AudioPlayer.pause()
            } else {
                speakerState.play()
                AudioPlayer.play(
                    text,
                    language,
                    onError = {
                        showSnackbar(FunnyApplication.resources.getString(R.string.snack_speak_error))
                    },
                    onComplete = {
                        speakerState.reset()
                    }
                )
            }
        }, modifier = Modifier
            .clip(CircleShape)
            .size(48.dp)
            .background(MaterialTheme.colorScheme.secondary)
    ) {
        FrameAnimationIcon(
            state = speakerState,
            contentDescription = stringResource(id = R.string.speak),
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }

}