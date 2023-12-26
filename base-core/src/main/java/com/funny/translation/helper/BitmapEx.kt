package com.funny.translation.helper

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.palette.graphics.Palette
import com.kyant.monet.getKeyColors

private const val TAG = "BitmapEx"

fun Bitmap.getKeyColors(count: Int = 1, defaultColor: Color, onSuccess: (Color) -> Unit) {
    Palette.from(this).maximumColorCount(count).generate {
        val colorInt = it?.swatches?.firstOrNull()?.rgb
        val color = if (colorInt == null) {
            Log.d(TAG, "getKeyColors: 取色失败, ${it?.swatches }}")
            defaultColor
        } else {
            Color(colorInt)
        }
        onSuccess(color)
    }
}

fun Bitmap.getKeyColors(count: Int) = this.asImageBitmap().getKeyColors(count)