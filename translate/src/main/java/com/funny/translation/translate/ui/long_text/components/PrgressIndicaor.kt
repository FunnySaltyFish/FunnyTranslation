package com.funny.translation.translate.ui.long_text.components

import androidx.annotation.FloatRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun TwoProgressIndicator(
    @FloatRange(0.0, 1.0) startedProgress: Float,
    @FloatRange(0.0, 1.0) finishedProgress: Float,
    modifier: Modifier = Modifier,
    startedColor: Color = ProgressIndicatorDefaults.linearColor.copy(0.5f),
    finishedColor: Color = ProgressIndicatorDefaults.linearColor,
    trackColor: Color = ProgressIndicatorDefaults.linearTrackColor,
    strokeCap: StrokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
) {
    Canvas(modifier = modifier
        .fillMaxWidth()
        .height(4.dp)) {
        val w = size.width
        val strokeWidth = size.height
        // 先画背景
        drawLinearIndicator(0f, 1f, trackColor, strokeWidth, strokeCap)
        // 再画已开始的进度
        drawLinearIndicator(0f, startedProgress, startedColor, strokeWidth, strokeCap)
        // 最后画已完成的进度
        drawLinearIndicator(0f, finishedProgress, finishedColor, strokeWidth, strokeCap)
    }
}

private fun DrawScope.drawLinearIndicator(
    startFraction: Float,
    endFraction: Float,
    color: Color,
    strokeWidth: Float,
    strokeCap: StrokeCap,
) {
    val width = size.width
    val height = size.height
    // Start drawing from the vertical center of the stroke
    val yOffset = height / 2

    val isLtr = layoutDirection == LayoutDirection.Ltr
    val barStart = (if (isLtr) startFraction else 1f - endFraction) * width
    val barEnd = (if (isLtr) endFraction else 1f - startFraction) * width

    // if there isn't enough space to draw the stroke caps, fall back to StrokeCap.Butt
    if (strokeCap == StrokeCap.Butt || height > width) {
        // Progress line
        drawLine(color, Offset(barStart, yOffset), Offset(barEnd, yOffset), strokeWidth)
    } else {
        // need to adjust barStart and barEnd for the stroke caps
        val strokeCapOffset = strokeWidth / 2
        val coerceRange = strokeCapOffset..(width - strokeCapOffset)
        val adjustedBarStart = barStart.coerceIn(coerceRange)
        val adjustedBarEnd = barEnd.coerceIn(coerceRange)

        if (abs(endFraction - startFraction) > 0) {
            // Progress line
            drawLine(
                color,
                Offset(adjustedBarStart, yOffset),
                Offset(adjustedBarEnd, yOffset),
                strokeWidth,
                strokeCap,
            )
        }
    }
}

@Composable
@Preview
fun PreviewTwoProgressIndicator() {
    TwoProgressIndicator(startedProgress = 0.8f, finishedProgress = 0.6f)
}