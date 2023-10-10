package com.funny.translation.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private const val TAG = "FixedSizeIcon"

/**
 * 当没有设置 size 时，无论对于 vector 还是 Bitmap 都设置默认的 24.dp 大小
  */
@Composable
fun FixedSizeIcon(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    val sizedModifier =
        if (modifier.all { it.javaClass.simpleName != "SizeElement" }) {
            modifier.size(24.dp)
        } else modifier
    Icon(painter, contentDescription, sizedModifier, tint)
}

@Composable
fun FixedSizeIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) = Icon(imageVector, contentDescription, modifier, tint)

private fun Modifier.defaultSizeFor(painter: Painter) =
    this.then(
        if (painter.intrinsicSize == Size.Unspecified || painter.intrinsicSize.isInfinite()) {
            DefaultIconSizeModifier
        } else {
            // if we haven't set a size, we give it a default size
            if (this.all {
                // replace with `it !is SizeElement`, I use reflection here because it's private
                it.javaClass.simpleName != "SizeElement"
            }) {
                DefaultIconSizeModifier
            } else Modifier
        }
    )

private fun Size.isInfinite() = width.isInfinite() && height.isInfinite()

// Default icon size, for icons with no intrinsic size information
private val DefaultIconSizeModifier = Modifier.size(24.dp)