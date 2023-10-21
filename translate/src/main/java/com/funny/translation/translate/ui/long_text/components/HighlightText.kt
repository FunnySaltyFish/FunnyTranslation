package com.funny.translation.translate.ui.long_text.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.funny.translation.debug.rememberStateOf
import com.funny.translation.translate.R
import com.funny.translation.translate.ui.long_text.Category
import com.funny.translation.translate.ui.long_text.ScreenState
import kotlinx.coroutines.launch

@Composable
internal fun ColumnScope.SourceTextPart(
    text: String,
    screenState: ScreenState,
    modifier: Modifier = Modifier,
    currentTransStartOffset: Int = -1,
    currentTransLength: Int = 0,
    translatingTextColor: Color = MaterialTheme.colorScheme.primary
) {
    Category(title = stringResource(id = R.string.source_text)) { expanded ->
        var maxLines by rememberStateOf(value = 8)
        LaunchedEffect(key1 = screenState) {
            maxLines = when (screenState) {
                ScreenState.Init -> 8
                ScreenState.Translating -> 8
                ScreenState.Result -> 2
            }
        }
        val textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, lineHeight = (14).sp)
        val translatingStyle = remember(textStyle) { textStyle.copy(color = translatingTextColor, fontWeight = FontWeight.Bold).toSpanStyle() }

        CompositionLocalProvider(LocalTextStyle provides textStyle) {
            AutoScrollHighlightedText(
                text = buildAnnotatedString {
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
                },
                highlightStartOffset = currentTransStartOffset,
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
    translatingTextColor: Color = MaterialTheme.colorScheme.primary
) {
    Category(title = stringResource(id = R.string.translate_result)) { expanded ->
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
                text = buildAnnotatedString {
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
                },
                highlightStartOffset = currentResultStartOffset,
                maxLines = if (expanded) 2 * maxLines else maxLines
            )
        }
    }
}

@Composable
private fun AutoScrollHighlightedText(
    text: AnnotatedString,
    highlightStartOffset: Int,
    maxLines: Int,
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    var layoutResult: TextLayoutResult? by rememberStateOf(value = null)
    val lineHeight = LocalTextStyle.current.lineHeight
    val density = LocalDensity.current
    val lineHeightInDp = remember(density) { with(density) { lineHeight.toDp()  } }

    LaunchedEffect(key1 = highlightStartOffset) {
        if (highlightStartOffset >= 0) {
            val offset = layoutResult?.getLineForOffset(highlightStartOffset)?.let {
                layoutResult?.getLineTop(it)
            } ?: return@LaunchedEffect
            scope.launch {
                // Log.d("AutoScrollHighlightText", "animateScrollTo: $offset")
                scrollState.animateScrollTo(offset.toInt())
            }
        }
    }

    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(0.dp, maxLines * lineHeightInDp)
            .animateContentSize()
            .verticalScroll(scrollState),
        onTextLayout = {
            layoutResult = it
        }
    )
}
