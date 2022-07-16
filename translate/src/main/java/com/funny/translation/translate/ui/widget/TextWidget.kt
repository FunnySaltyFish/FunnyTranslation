package com.funny.translation.translate.ui.widget

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun HeadingText(
    text : String
) {
    Text(
        modifier = Modifier.semantics { heading() },
        text = text,
        fontSize = 32.sp,
        fontWeight = FontWeight.ExtraBold
    )
}