package com.funny.translation.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.funny.translation.theme.isLight
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun SystemBarSettings(
    hideStatusBar: Boolean = false,
) {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = MaterialTheme.colorScheme.isLight
    SideEffect {
        systemUiController.setStatusBarColor(Color.Transparent, darkIcons = useDarkIcons)
        if (hideStatusBar) {
            systemUiController.isStatusBarVisible = false
        }
    }
}