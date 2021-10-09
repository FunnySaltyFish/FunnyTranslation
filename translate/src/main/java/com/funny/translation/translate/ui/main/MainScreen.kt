package com.funny.translation.translate.ui.main

import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.funny.cmaterialcolors.MaterialColors
import com.funny.translation.trans.*
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.R
import com.funny.translation.translate.ui.bean.RoundCornerConfig
import com.funny.translation.translate.ui.widget.*
import com.funny.translation.translate.utils.AudioPlayer
import com.funny.translation.translate.utils.ClipBoardUtil
import kotlinx.coroutines.launch

private const val TAG = "MainScreen"

@ExperimentalAnimationApi
@Composable
fun MainScreen(
    showSnackbar : (String) -> Unit,
) {
    val vm : MainViewModel = viewModel()
    val transText by vm.translateText.observeAsState("")
    val sourceLanguage by vm.sourceLanguage.observeAsState()
    val targetLanguage by vm.targetLanguage.observeAsState()

    val resultList by vm.resultList.observeAsState()
    val translateProgress by vm.progress.observeAsState()

    val allEngines by vm.allEngines.observeAsState()
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .padding(16.dp, 12.dp)
            .fillMaxSize()
            .scrollable(scrollState, Orientation.Vertical)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        EngineSelect(allEngines!!)
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
                offset = DpOffset(220.dp,8.dp),
                updateLanguage = {
                    vm.targetLanguage.value = it
                }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        InputText(text = transText, updateText = { vm.translateText.value = it })
        Spacer(modifier = Modifier.height(12.dp))
        TranslateButton(translateProgress!!) {
            if (vm.selectedEngines.isEmpty()){
                showSnackbar( FunnyApplication.resources.getString(R.string.snack_no_engine_selected))
                return@TranslateButton
            }
            vm.translate()
        }
        Spacer(modifier = Modifier.height(18.dp))
        TranslationList(resultList!!, showSnackbar)
    }

}

@ExperimentalAnimationApi
@Composable
fun EngineSelect(
    engines : ArrayList<TranslationEngine> = arrayListOf(),
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//        var expanded by remember {
//            mutableStateOf(false)
//        }
        Box(modifier = Modifier
            //.apply { if (!expanded) height(40.dp) else wrapContentHeight() }
            .fillMaxWidth()
            .animateContentSize()
        ){
            LazyRow(
                horizontalArrangement = spacedBy(8.dp),
            ) {
                itemsIndexed(engines){ index, task ->
                    //临时出来的解决措施，因为ArrayList单个值更新不会触发LiveData的更新。更新自己
                    var selected : Boolean by remember {
                        mutableStateOf(task.selected)
                    }
                    SelectableChip(selected = selected, text = task.name) {
                        engines[index].selected = !task.selected
                        selected = !selected
                        //updateView(tasks)
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageSelect(
    language : Language,
    languages : List<Language>,
    offset: DpOffset = DpOffset(2.dp,8.dp),
    updateLanguage : (Language)->Unit,

) {
    var expanded by remember {
        mutableStateOf(false)
    }
    RoundCornerButton(text = language.displayText){
        expanded = true
    }
    DropdownMenu(expanded = expanded , onDismissRequest = { expanded = false }, offset = offset) {
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

@Composable
fun TranslationList(
    resultList: List<TranslationResult>,
    showSnackbar: (String) -> Unit
) {
    val size = resultList.size
    LazyColumn(
        verticalArrangement = spacedBy(4.dp)
    ) {
        itemsIndexed(resultList, key = {i,r->r.engineName}){ index, result->
            //Log.d(TAG, "TranslationList: $result")
            TranslationItem(result = result, roundCornerConfig = when (index){
                0 ->  if (size==1) RoundCornerConfig.All else RoundCornerConfig.Top
                size-1 -> RoundCornerConfig.Bottom
                else -> RoundCornerConfig.None
            },showSnackbar = showSnackbar)
        }
    }
}

@Composable
fun MainAppbar() {
    TopAppBar(
        title = {
            Text(text = stringResource(id = R.string.app_name))
        }
    )
}

@Composable
fun TranslateButton(
    progress : Int = 100,
    onClick : ()->Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Button(onClick = onClick, shape = CircleShape, modifier=Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.Transparent
        ), contentPadding = PaddingValues(0.dp)) {
            Box(modifier = Modifier.fillMaxWidth()){
                val p = if(progress == 0) 100 else progress
                    Box(modifier = Modifier
                        .fillMaxWidth(p / 100f)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.primary)
                    )
                Text(text = stringResource(id = R.string.translate), color = Color.White, modifier = Modifier.align(Alignment.Center), fontSize = 22.sp)
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
            Text(text = result.engineName , color = MaterialColors.Grey600, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = result.basicResult.trans,
                color = MaterialTheme.colors.secondary,
                fontSize = 24.sp,
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
                         AudioPlayer.play(result.basicResult.trans.trim(), result.targetLanguage!!
                         ) {
                             showSnackbar(FunnyApplication.resources.getString(R.string.snack_speak_error))
                         }
                    }, modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.secondary)
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

