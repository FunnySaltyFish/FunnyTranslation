package com.funny.translation.translate.ui.main

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.funny.cmaterialcolors.MaterialColors
import com.funny.translation.trans.*
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.R
import com.funny.translation.translate.ui.bean.RoundCornerConfig
import com.funny.translation.translate.ui.widget.*
import com.funny.translation.translate.utils.AudioPlayer
import com.funny.translation.translate.utils.ClipBoardUtil
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.insets.statusBarsHeight
import com.google.accompanist.insets.systemBarsPadding
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "MainScreen"

//@ExperimentalAnimationApi
//@ExperimentalMaterialApi
//@Composable
//fun MainScreen(
//    showSnackbar : (String) -> Unit,
//    activityViewModel: ActivityViewModel
//) {
//    val vm : MainViewModel = viewModel()
//    val bindEngines by vm.bindEngines.observeAsState()
//    val jsEngines by vm.jsEngines.collectAsState(arrayListOf())
//    BottomSheetScaffold(
//        modifier = Modifier.fillMaxHeight(),
//        sheetContent = {
//            EngineSelect(bindEngines!!, jsEngines?: arrayListOf(), updateJsEngine = {
//                val temp = arrayListOf<TranslationEngine>()
//                temp.addAll(bindEngines!!)
//                temp.addAll(jsEngines)
//                vm.allEngines = temp
//            })
//        },
//        sheetPeekHeight = 20.dp,
//        sheetShape = RoundedCornerShape(topEnd = 36.dp, topStart = 36.dp),
//        backgroundColor = Color.White
//    ) {
//        MainScreenContent(showSnackbar = showSnackbar, activityViewModel = activityViewModel)
//    }
//}

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun MainScreen(
    showSnackbar: (String) -> Unit
) {
    val vm: MainViewModel = viewModel()
    val scope = rememberCoroutineScope()

    val transText by vm.translateText.observeAsState("")
    val sourceLanguage by vm.sourceLanguage.observeAsState()
    val targetLanguage by vm.targetLanguage.observeAsState()

    val resultList by vm.resultList.observeAsState()
    val translateProgress by vm.progress.observeAsState()

    val bindEngines by vm.bindEngines.observeAsState()
    val jsEngines by vm.jsEngines.collectAsState(arrayListOf())

    val lifecycleEventObserver = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_DESTROY -> {
                vm.saveData()
                Log.d(TAG, "MainScreen: 被销毁状态")
            }
            else -> Unit
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(key1 = true) {
        lifecycle.addObserver(lifecycleEventObserver)
        onDispose {
            vm.saveData()
            lifecycle.removeObserver(lifecycleEventObserver)
        }
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var expandEngineSelect by remember {
            mutableStateOf(false)
        }
        val swipeableState = rememberSwipeableState(initialValue = ExpandState.CLOSE)
        Spacer(modifier = Modifier.statusBarsHeight())
        // 这里的实现很不优雅，强行更改了viewModel的allEngines，
        // 主要是 Flow 用的不熟
        AnimatedVisibility(visible = swipeableState.currentValue == ExpandState.OPEN || expandEngineSelect) {
            EngineSelect(
                modifier = Modifier.padding(8.dp),
                bindEngines!!, jsEngines, updateJsEngine = {
                    val temp = arrayListOf<TranslationEngine>()
                    temp.addAll(bindEngines!!)
                    temp.addAll(jsEngines)
                    vm.allEngines = temp
                },
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier
            .clip(CircleShape)
            .fillMaxWidth(0.4f)
            .height(12.dp)
            .background(MaterialTheme.colors.secondary)
            .clickable { expandEngineSelect = !expandEngineSelect }
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            LanguageSelect(
                language = sourceLanguage!!,
                languages = allLanguages,
                updateLanguage = {
                    vm.sourceLanguage.value = it
                }
            )
            ExchangeButton {
                Log.d(TAG, "MainScreen: clicked")
                val temp = sourceLanguage
                vm.sourceLanguage.value = targetLanguage
                vm.targetLanguage.value = temp
            }
            LanguageSelect(
                language = targetLanguage!!,
                languages = allLanguages,
                updateLanguage = {
                    vm.targetLanguage.value = it
                }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        InputText(text = transText, updateText = { vm.translateText.value = it })
        Spacer(modifier = Modifier.height(12.dp))
        TranslateButton(translateProgress!!) {
            if (vm.selectedEngines.isEmpty()) {
                showSnackbar(FunnyApplication.resources.getString(R.string.snack_no_engine_selected))
                return@TranslateButton
            }
            if(vm.isTranslating()) vm.translate()
            else{
                vm.cancel()
                showSnackbar("当前翻译已终止")
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        TranslationList(resultList!!, showSnackbar)
    }


}

@ExperimentalAnimationApi
@Composable
fun EngineSelect(
    modifier: Modifier,
    bindEngines: ArrayList<TranslationEngine> = arrayListOf(),
    jsEngines: List<TranslationEngine> = arrayListOf(),
    updateJsEngine: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(id = R.string.bind_engine),
            fontWeight = W600
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            mainAxisSpacing = 12.dp,
            crossAxisSpacing = 8.dp
        ) {
            bindEngines.forEachIndexed { index, task ->
                //临时出来的解决措施，因为ArrayList单个值更新不会触发LiveData的更新。更新自己
                SelectableChip(initialSelect = task.selected, text = task.name) {
                    bindEngines[index].selected = !task.selected
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
                mainAxisSpacing = 12.dp,
                crossAxisSpacing = 8.dp
            ) {
                jsEngines.forEachIndexed { index, task ->
                    //临时出来的解决措施，因为ArrayList单个值更新不会触发LiveData的更新。更新自己
                    SelectableChip(initialSelect = task.selected, text = task.name) {
                        jsEngines[index].selected = !task.selected
                        updateJsEngine()
                    }
                }
            }
        }

//        LazyRow(
//            horizontalArrangement = spacedBy(8.dp),
//        ) {
//            itemsIndexed(jsEngines) { index, task ->
//                //临时出来的解决措施，因为ArrayList单个值更新不会触发LiveData的更新。更新自己
//                var selected: Boolean by remember {
//                    mutableStateOf(task.selected)
//                }
//                SelectableChip(initialSelect = selected, text = task.name) {
//                    jsEngines[index].selected = !task.selected
//                    selected = !selected
//                    //updateView(tasks)
//                    updateJsEngine()
//                }
//            }
//        }
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

@Composable
fun TranslationList(
    resultList: List<TranslationResult>,
    showSnackbar: (String) -> Unit
) {
    val size = resultList.size
    LazyColumn(
        verticalArrangement = spacedBy(4.dp)
    ) {
        itemsIndexed(resultList, key = { _, r -> r.engineName }) { index, result ->
            //Log.d(TAG, "TranslationList: $result")
            TranslationItem(
                result = result, roundCornerConfig = when (index) {
                    0 -> if (size == 1) RoundCornerConfig.All else RoundCornerConfig.Top
                    size - 1 -> RoundCornerConfig.Bottom
                    else -> RoundCornerConfig.None
                }, showSnackbar = showSnackbar
            )
        }
        item { Spacer(modifier = Modifier.height(64.dp)) }
    }
}

@Composable
fun TranslateButton(
    progress: Int = 100,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onClick,
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Transparent
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                val p = if (progress == 0) 100 else progress
                Box(
                    modifier = Modifier
                        .fillMaxWidth(p / 100f)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.primary)
                )
                Text(
                    text = stringResource(id = R.string.translate),
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 22.sp
                )
            }
        }
    }
}


@Composable
fun TranslationItem(
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
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.surface, shape = shape)
            .padding(12.dp)
            .animateContentSize()

    ) {
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = result.engineName, color = MaterialColors.Grey600, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            val fontSize = when (result.basicResult.trans.length) {
                in 0..25 -> 24
                in 26..50 -> 20
                in 50..70 -> 16
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
                        .size(36.dp)
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
                        .size(36.dp)
                        .clip(CircleShape)
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
                        tint = Color.White
                    )
                }
                result.details?.let {
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                        ExpandMoreButton {

                        }
                    }

                }
            }
        }
    }
}

enum class ExpandState{
    OPEN,
    CLOSE
}