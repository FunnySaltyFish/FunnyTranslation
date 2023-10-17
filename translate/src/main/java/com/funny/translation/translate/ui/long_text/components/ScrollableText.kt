package com.funny.translation.translate.ui.long_text.components

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

private val TEXT = "策划书测试啊大家啊发发 静安分局安家费积分假按揭激发发".repeat(100)
@Composable
fun ScrollableText() {
    Text(text = TEXT, modifier = Modifier.verticalScroll(rememberScrollState()), maxLines = 8)
}