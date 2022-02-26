package com.funny.translation.translate.ui.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AboutScreen() {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)){
        item {
            Text("关于页面")
        }
    }
}