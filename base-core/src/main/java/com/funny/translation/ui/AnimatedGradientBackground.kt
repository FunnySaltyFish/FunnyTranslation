package com.funny.translation.ui

import androidx.compose.animation.core.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Stable
fun Modifier.animatedGradientBackground(
    color1: Color,
    color2: Color,
) = composed {
    val infiniteTransition = rememberInfiniteTransition()
    val offset by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = InfiniteRepeatableSpec(
            animation = TweenSpec(
                durationMillis = 10000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse,
        )
    )

    Modifier.drawWithContent {
        drawRect(
            brush = Brush.linearGradient(
                0f to color1,
                offset to color2,
                1f to color1,
            )
        )
        drawContent()
    }

}