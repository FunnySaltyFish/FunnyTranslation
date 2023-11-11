package com.funny.translation.translate.ui.long_text.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.compose.ai.token.TokenCounter
import com.funny.translation.debug.rememberStateOf
import com.funny.translation.helper.ResultEffect
import com.funny.translation.translate.LocalNavController
import com.funny.translation.translate.R
import com.funny.translation.translate.ui.long_text.Category
import com.funny.translation.translate.ui.long_text.KEY_EDITED_SOURCE_TEXT
import com.funny.translation.translate.ui.long_text.ScreenState
import com.funny.translation.translate.ui.long_text.TextEditorAction
import com.funny.translation.translate.ui.long_text.navigateToTextEdit
import com.funny.translation.ui.FixedSizeIcon
import kotlinx.coroutines.launch

@Composable
internal fun ColumnScope.SourceTextPart(
    modifier: Modifier = Modifier,
    text: String,
    // updateSourceText 实际还包括对数据库的修改，因此只在弹出对话框按下确定后，才应该执行
    updateSourceText: (String) -> Unit = {},
    screenState: ScreenState,
    currentTransStartOffset: Int = -1,
    currentTransLength: Int = 0,
    tokenCounter: TokenCounter,
    translatingTextColor: Color = MaterialTheme.colorScheme.primary
) {
    val navController = LocalNavController.current
    Category(
        title = stringResource(id = R.string.source_text),
        helpText = stringResource(id = R.string.source_text_help),
        extraRowContent = {
            if (screenState == ScreenState.Init) {
                ResultEffect<String>(navController = navController, resultKey = KEY_EDITED_SOURCE_TEXT) {
                    if (it.isNotBlank()) {
                        updateSourceText(it)
                    }
                }
                FixedSizeIcon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier
                    .size(16.dp)
                    .clickable {
                        navController.navigateToTextEdit(
                            TextEditorAction.UpdateSourceText(text)
                        )
                    })
            }
            TokenNum(modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.End), tokenCounter = tokenCounter, text = text)
        }
    ) { expanded ->
        var maxLines by rememberStateOf(value = 8)
        LaunchedEffect(key1 = screenState) {
            maxLines = when (screenState) {
                ScreenState.Init -> 8
                ScreenState.Translating -> 8
                ScreenState.Result -> 8
            }
        }
        val textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, lineHeight = (14).sp)
        val translatingStyle = remember(textStyle) { textStyle.copy(color = translatingTextColor, fontWeight = FontWeight.Bold).toSpanStyle() }

        CompositionLocalProvider(LocalTextStyle provides textStyle) {
            AutoScrollHighlightedText(
                textProvider = {
                    buildAnnotatedString {
                        if (currentTransStartOffset >= 0 && currentTransLength > 0) {
                            append(text.substring(0, currentTransStartOffset))
                            val end = currentTransStartOffset + currentTransLength
                            if (end < text.length) {
                                withStyle(style = translatingStyle) {
                                    append(
                                        text.substring(currentTransStartOffset, end)
                                    )
                                }
                                append(text.substring(end))
                            } else {
                                withStyle(style = translatingStyle) {
                                    append(
                                        text.substring(currentTransStartOffset)
                                    )
                                }
                            }
                        } else {
                            append(text)
                        }
                    }
                },
                highlightStartOffsetProvider = { currentTransStartOffset },
                maxLines = if (expanded) 2 * maxLines else maxLines
            )
        }
    }
}

@Composable
internal fun ColumnScope.ResultTextPart(
    text: String,
    screenState: ScreenState,
    modifier: Modifier = Modifier,
    currentResultStartOffset: Int = -1,
    tokenCounter: TokenCounter,
    translatingTextColor: Color = MaterialTheme.colorScheme.primary
) {
    Category(
        title = stringResource(id = R.string.translate_result),
        helpText = stringResource(id = R.string.translate_result_help),
        extraRowContent = {
            TokenNum(modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.End), tokenCounter = tokenCounter, text = text)
        }
    ) { expanded ->
        var maxLines by rememberStateOf(value = 8)
        LaunchedEffect(key1 = screenState) {
            maxLines = when (screenState) {
                ScreenState.Init -> 8
                ScreenState.Translating -> 16
                ScreenState.Result -> 30
            }
        }
        val textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, lineHeight = (14).sp)
        val highlightStyle = remember(textStyle) { textStyle.copy(color = translatingTextColor, fontWeight = FontWeight.Bold).toSpanStyle() }
        CompositionLocalProvider(LocalTextStyle provides textStyle) {
            AutoScrollHighlightedText(
                textProvider = {
                    buildAnnotatedString {
                        if (currentResultStartOffset >= 0) {
                            append(text.substring(0, currentResultStartOffset))
                            withStyle(style = highlightStyle) {
                                append(
                                    text.substring(currentResultStartOffset)
                                )
                            }
                        } else {
                            append(text)
                        }
                    }
                },
                highlightStartOffsetProvider = { currentResultStartOffset },
                maxLines = if (expanded) 2 * maxLines else maxLines
            )
        }
    }
}

@Composable
private fun AutoScrollHighlightedText(
    textProvider: () -> AnnotatedString,
    highlightStartOffsetProvider: () -> Int,
    maxLines: Int,
) {
    val scope = rememberCoroutineScope()
    val lineHeight = LocalTextStyle.current.lineHeight
    val density = LocalDensity.current
    val lineHeightInDp = remember(density) { with(density) { lineHeight.toDp()  } }
    val text = textProvider()
    val longTextState = rememberLongTextState(text = text, lazyListState = rememberLazyListState())
    val highlightStartOffset = highlightStartOffsetProvider()
    val heightAnim = remember {
        Animatable(maxLines * lineHeightInDp.value)
    }
    LaunchedEffect(key1 = maxLines) {
        heightAnim.animateTo(maxLines * lineHeightInDp.value)
    }
    LaunchedEffect(key1 = highlightStartOffset) {
        if (highlightStartOffset >= 0) {
            scope.launch {
                longTextState.scrollToIndex(highlightStartOffset)
            }
        }
    }
    LongText(
        modifier = Modifier
            .heightIn(0.dp, heightAnim.value.dp),
        state = longTextState
    )
}
