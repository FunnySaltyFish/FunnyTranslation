package com.funny.compose.ai.utils

import android.animation.ArgbEvaluator
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

fun getColorAtProgress(progress: Float, start: Color, end: Color, evaluator: ArgbEvaluator): Color {
    return Color(evaluator.evaluate(progress, start.toArgb(), end.toArgb()) as Int)
}