package com.funny.translation.translate.ui.main

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.jetsetting.core.ui.SimpleDialog
import com.funny.translation.AppConfig
import com.funny.translation.GlobalTranslationConfig
import com.funny.translation.helper.ClipBoardUtil
import com.funny.translation.helper.toastOnUi
import com.funny.translation.translate.*
import com.funny.translation.translate.R
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.ui.widget.*
import com.funny.translation.translate.utils.AudioPlayer
import com.funny.translation.ui.MarkdownText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@Composable
fun MainPartTranslating(vm: MainViewModel) {
    val quitAlertDialog = remember { mutableStateOf(false) }
    SimpleDialog(
        openDialogState = quitAlertDialog,
        title = stringResource(id = R.string.tip),
        message = stringResource(id = R.string.quit_translating_alert),
        confirmButtonAction = {
            vm.cancel()
            vm.updateMainScreenState(MainScreenState.Inputting)
        }
    )

    val goBack = remember {
        {
            if (vm.isTranslating()) {
                quitAlertDialog.value = true
            } else {
                vm.cancel()
                vm.updateMainScreenState(MainScreenState.Inputting)
            }
        }
    }

    BackHandler(onBack = goBack)
    CommonPage(
        navigationIcon = {
            CommonNavBackIcon(navigateBackAction = goBack)
        }
    ) {
        TranslateProgress(progress = vm.progress)
        SourceTextPart(
            modifier = Modifier.fillMaxWidth(0.88f),
            sourceText = vm.translateText,
            sourceLanguage = vm.sourceLanguage
        )
        Spacer(modifier = Modifier.height(8.dp))
        ResultList(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            resultList = vm.resultList,
            doFavorite = vm::doFavorite
        )
    }
}

@Composable
private fun TranslateProgress(
    progress: Float
) {
    // 这个 99 （而不是 100） 是为了解决浮点数累加导致的问题，比如 33.3 + 33.3 + 33.3 = 99.9 != 100
    // >= 99 就认为是翻译完了
    AnimatedVisibility(visible = progress < 99) {
        LinearProgressIndicator(
            progress = progress / 100,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SourceTextPart(
    modifier: Modifier,
    sourceText: String,
    sourceLanguage: Language,
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.source_text) + "(${sourceLanguage.displayText})"
            )
            SpeakButton(
                text = sourceText,
                language = sourceLanguage,
                tint = MaterialTheme.colorScheme.onBackground
            )
            CopyButton(text = sourceText, tint = MaterialTheme.colorScheme.onBackground)
        }
        SwipeableText(text = sourceText, modifier = Modifier.fillMaxWidth())
    }
}

// TODO 会员详细

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SwipeableText(
    text: String,
    modifier: Modifier,
) {
    val state = rememberSwipeableState(2)
    LaunchedEffect(key1 = state.currentValue) {
        Log.d("SwipeableText", "SwipeableText: state.currentValue: ${state.currentValue}")
    }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .swipeable(
                    state = state,
                    anchors = mapOf(0f to 2, 100f to 8),
                    orientation = Orientation.Vertical,
                    thresholds = { _, _ -> FractionalThreshold(0.3f) },
                )
                .animateContentSize(),
            overflow = TextOverflow.Ellipsis,
            maxLines = state.currentValue
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .height(2.dp)
                .fillMaxWidth(0.25f)
                .background(Color.LightGray)
                .clip(CircleShape)
        )
    }

}

@Composable
internal fun SpeakButton(
    modifier: Modifier = Modifier,
    text: String,
    language: Language,
    tint: Color = MaterialTheme.colorScheme.primary
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
        modifier = modifier.size(48.dp),
        onClick = {
            if (text == AudioPlayer.currentPlayingText) {
                speakerState.reset()
                AudioPlayer.pause()
            } else {
                speakerState.play()
                AudioPlayer.playOrPause(
                    text,
                    language,
                    onError = {
                        appCtx.toastOnUi(FunnyApplication.resources.getString(R.string.snack_speak_error))
                    },
                    onComplete = {
                        speakerState.reset()
                    }
                )
            }
        }
    ) {
        FrameAnimationIcon(
            state = speakerState,
            contentDescription = stringResource(id = R.string.speak),
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
internal fun CopyButton(
    modifier: Modifier = Modifier,
    text: String,
    tint: Color
) {
    IconButton(
        onClick = {
            ClipBoardUtil.copy(appCtx, text)
            appCtx.toastOnUi(FunnyApplication.resources.getString(R.string.snack_finish_copy))
        },
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            Icons.Default.CopyAll,
            contentDescription = stringResource(id = R.string.copy_content),
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun ResultList(
    modifier: Modifier,
    resultList: List<TranslationResult>,
    doFavorite: (Boolean, TranslationResult) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        itemsIndexed(resultList, key = { _, r -> r.engineName }) { _, result ->
            ResultItem(
                modifier = Modifier.fillMaxWidth(),
                result = result,
                doFavorite = doFavorite
            )
        }
    }
}

@Composable
private fun ResultItem(
    modifier: Modifier,
    result: TranslationResult,
    doFavorite: (Boolean, TranslationResult) -> Unit,
) {
    val offsetAnim = remember { Animatable(100f) }
    LaunchedEffect(Unit) {
        offsetAnim.animateTo(0f)
    }
    Column(
        modifier = modifier
            .offset { IntOffset(offsetAnim.value.roundToInt(), 0) }
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(start = 20.dp, end = 20.dp, bottom = 16.dp, top = 4.dp)
            .animateContentSize()
    ) {
        var expandDetail by rememberSaveable(key = AppConfig.sExpandDetailByDefault.value.toString()) {
            mutableStateOf(!result.detailText.isNullOrEmpty() && AppConfig.sExpandDetailByDefault.value)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = result.engineName,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.W500
            )

            // 如果有详细释义，则显示展开按钮
            if (!result.detailText.isNullOrEmpty()) {
                ExpandMoreButton(modifier = Modifier.offset(24.dp), expand = expandDetail, tint = MaterialTheme.colorScheme.primary) {
                    expandDetail = it
                }
            }
            // 收藏、朗读、复制三个图标
            var favorite by rememberFavoriteState(result = result)
            IconButton(onClick = {
                doFavorite(favorite, result)
                favorite = !favorite
            }, modifier = Modifier.offset(x = 16.dp)) {
                Icon(
                    imageVector = if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = stringResource(id = R.string.favorite),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            SpeakButton(
                modifier = Modifier.offset(8.dp),
                text = result.basicResult.trans,
                language = result.targetLanguage!!
            )
            CopyButton(
                text = result.basicResult.trans,
                tint = MaterialTheme.colorScheme.primary
            )
        }

        SelectionContainer {
            Text(
                text = result.basicResult.trans,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 16.sp,
            )
        }
        if (expandDetail) {
            Divider(modifier = Modifier.padding(top = 4.dp))
            MarkdownText(
                markdown = result.detailText!!,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                selectable = true
            )
        }
    }
}

@Composable
private fun rememberFavoriteState(
    result: TranslationResult
): MutableState<Boolean> {
    val state = remember { mutableStateOf(false) }
    LaunchedEffect(key1 = Unit) {
        withContext(Dispatchers.IO) {
            if (!GlobalTranslationConfig.isValid()) return@withContext
            state.value = appDB.transFavoriteDao.count(
                GlobalTranslationConfig.sourceString!!,
                result.basicResult.trans,
                GlobalTranslationConfig.sourceLanguage!!.id,
                GlobalTranslationConfig.targetLanguage!!.id,
                result.engineName
            ) > 0
        }
    }
    return state
}