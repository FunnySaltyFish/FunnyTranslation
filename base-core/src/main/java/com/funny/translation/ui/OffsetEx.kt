package com.funny.translation.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset

fun IntOffset.asOffset() = Offset(x.toFloat(), y.toFloat())

fun Offset.asIntOffset() = IntOffset(x.toInt(), y.toInt())