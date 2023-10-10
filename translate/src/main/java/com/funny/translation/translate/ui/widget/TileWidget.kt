package com.funny.translation.translate.ui.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.jetsetting.core.ui.throttleClick
import com.funny.translation.ui.FixedSizeIcon

@Composable
fun RadioTile(
    text: String,
    selected: Boolean,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .throttleClick(onClick = onClick)
            .padding(8.dp)
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.W700,
            modifier = Modifier.weight(1f),
            color = textColor
        )
        RadioButton(selected = selected, onClick = onClick)
    }
}

@Composable
fun ArrowTile(
    text: String,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .throttleClick(onClick = onClick)
            .padding(8.dp)
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.W700,
            modifier = Modifier.weight(1f),
            color = textColor
        )
        FixedSizeIcon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = textColor)
    }
}