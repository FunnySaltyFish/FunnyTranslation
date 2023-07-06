package com.funny.translation.translate.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun UpperPartBackground(
    modifier: Modifier = Modifier,
    cornerSizeProvider: () -> Dp = { 40.dp },
    content: @Composable() (ColumnScope.() -> Unit)
) {
    val color = MaterialTheme.colorScheme.surface
    val cornerSize = cornerSizeProvider()
    val shape =
        RoundedCornerShape(bottomStart = cornerSize, bottomEnd = cornerSize)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = color, shape = shape)
            .clip(shape),
        content = content,
        horizontalAlignment = Alignment.CenterHorizontally
    )
}