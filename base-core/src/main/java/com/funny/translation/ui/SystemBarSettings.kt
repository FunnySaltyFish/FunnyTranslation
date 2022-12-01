package com.funny.translation.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.funny.translation.AppConfig
import com.funny.translation.theme.isLight
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun SystemBarSettings() {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = MaterialTheme.colorScheme.isLight
    val navigationBarColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
    SideEffect {
        systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = useDarkIcons)
        systemUiController.setNavigationBarColor(
            if (!useDarkIcons) Color.Transparent else navigationBarColor,
            darkIcons = useDarkIcons
        )
    }

    LaunchedEffect(AppConfig.sHideBottomNav.value) {
        systemUiController.isNavigationBarVisible =
            !AppConfig.sHideBottomNav.value
    }

    LaunchedEffect(AppConfig.sHideStatusBar.value) {
        systemUiController.isStatusBarVisible = !AppConfig.sHideStatusBar.value
    }
}