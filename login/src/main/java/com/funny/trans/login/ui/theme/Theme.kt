package com.funny.trans.login.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

val lightScheme = lightColorScheme(

)

val darkScheme = darkColorScheme(

)

@Composable
fun LoginTheme (
    dark: Boolean = isSystemInDarkTheme(),
    dynamic: Boolean = Build. VERSION.SDK_INT >= Build.VERSION_CODES.S,
    content: @Composable () -> Unit
) {
    // ColorScheme 配置以及 MaterialTheme
    val colorScheme = if (dynamic) {
        val context = LocalContext.current
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (dark) darkScheme else lightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}