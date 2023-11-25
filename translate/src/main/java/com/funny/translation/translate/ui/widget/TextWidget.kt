package com.funny.translation.translate.ui.widget

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun HeadingText(
    text : String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier.semantics { heading() },
        text = text,
        fontSize = 32.sp,
        fontWeight = FontWeight.ExtraBold
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NumberChangeAnimatedText(
    modifier: Modifier = Modifier,
    text: String,
    textSpacing: TextUnit = LocalTextStyle.current.letterSpacing,
    textSize: TextUnit = LocalTextStyle.current.fontSize,
    textColor: Color = LocalTextStyle.current.color,
    textWeight: FontWeight = LocalTextStyle.current.fontWeight ?: FontWeight.Normal,
) {
    val density = LocalDensity.current
    val textPadding = remember(textSpacing) {
        PaddingValues(
            horizontal = with(density) { textSpacing.toDp() },
        )
    }
    Row(modifier = modifier) {
        text.forEach {
            AnimatedContent(
                targetState = it,
                transitionSpec = {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up) togetherWith
                            fadeOut() + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Up)
                },
                label = ""
            ) { char ->
                Text(text = char.toString(), modifier = modifier.padding(textPadding), fontSize = textSize, color = textColor, fontWeight = textWeight)
            }
        }
    }
}

/**
 * 用于显示数字变化的动画
 * @author FunnySaltyFish
 * @param modifier Modifier
 * @param startAnim Boolean
 * @param number Int
 * @param durationMills Int
 * @param textPadding PaddingValues
 * @param textSize TextUnit
 * @param textColor Color
 * @param textWeight FontWeight
 */
@Composable
fun AutoIncreaseAnimatedNumber(
    modifier: Modifier = Modifier,
    startAnim: Boolean = true,
    number: Int,
    durationMills: Int = 10000,
    textSpacing: TextUnit = LocalTextStyle.current.letterSpacing,
    textSize: TextUnit = LocalTextStyle.current.fontSize,
    textColor: Color = LocalTextStyle.current.color,
    textWeight: FontWeight = LocalTextStyle.current.fontWeight ?: FontWeight.Normal,
) {
    // 动画，Animatable 相关介绍可以见 https://compose.funnysaltyfish.fun/docs/design/animation/animatable?source=trans
    val animatedNumber = remember {
        androidx.compose.animation.core.Animatable(0f)
    }
    // 数字格式化后的长度
    val l = remember {
        number.toString().length
    }

    // Composable 进入 Composition 阶段，且 startAnim 为 true 时开启动画
    LaunchedEffect(number, startAnim) {
        if (startAnim)
            animatedNumber.animateTo(
                targetValue = number.toFloat(),
                animationSpec = tween(durationMillis = durationMills)
            )
    }

    NumberChangeAnimatedText(
        modifier = modifier,
        text = "%0${l}d".format(animatedNumber.value.roundToInt()),
        textSpacing = textSpacing,
        textColor = textColor,
        textSize = textSize,
        textWeight = textWeight
    )
}

@Composable
fun AutoResizedText(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = MaterialTheme.typography.headlineLarge,
    color: Color = style.color,
    maxLines: Int = Int.MAX_VALUE,
    byHeight: Boolean = true,
) {
    var resizedTextStyle by remember { mutableStateOf(style) }
    var shouldDraw by remember { mutableStateOf(false) }

    val defaultFontSize = MaterialTheme.typography.headlineLarge.fontSize

    Text(
        text = text,
        color = color,
        modifier = modifier.drawWithContent {
            if (shouldDraw) {
                drawContent()
            }
        },
        softWrap = maxLines > 1,
        style = resizedTextStyle.copy(fontSize = resizedTextStyle.fontSize),
        onTextLayout = { result ->
            if (if (byHeight) result.didOverflowHeight else result.didOverflowWidth) {
                if (style.fontSize.isUnspecified) {
                    resizedTextStyle = resizedTextStyle.copy(
                        fontSize = defaultFontSize
                    )
                }
                resizedTextStyle = resizedTextStyle.copy(
                    fontSize = resizedTextStyle.fontSize * 0.95
                )
            } else {
                shouldDraw = true
            }
        },
        maxLines = maxLines
    )
}

@Composable
fun HintText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
    fontSize: TextUnit = MaterialTheme.typography.bodySmall.fontSize,
) {
    Text(
        text = text,
        color = color,
        fontSize = fontSize,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        textAlign = TextAlign.Center
    )
}

val TextUnitSaver = object : Saver<TextUnit, Float> {
    override fun restore(value: Float): TextUnit = value.sp
    override fun SaverScope.save(value: TextUnit) = value.value
}