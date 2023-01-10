package com.funny.translation.translate.ui.widget

import androidx.compose.animation.*
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
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
    textPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
    textSize: TextUnit = 24.sp,
    textColor: Color = Color.Black,
    textWeight: FontWeight = FontWeight.Normal,
) {
    Row(modifier = modifier) {
        text.forEach {
            AnimatedContent(
                targetState = it,
                transitionSpec = {
                    slideIntoContainer(AnimatedContentScope.SlideDirection.Up) with
                            fadeOut() + slideOutOfContainer(AnimatedContentScope.SlideDirection.Up)
                }
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
    textPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
    textSize: TextUnit = 24.sp,
    textColor: Color = Color.Black,
    textWeight: FontWeight = FontWeight.Normal
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
        textPadding = textPadding,
        textColor = textColor,
        textSize = textSize,
        textWeight = textWeight
    )
}
