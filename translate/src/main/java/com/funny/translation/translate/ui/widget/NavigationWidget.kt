package com.funny.translation.translate.ui.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.funny.translation.AppConfig
import com.funny.translation.translate.ui.screen.TranslateScreen

private const val TAG = "CustomNavigation"

@Composable
fun <T : Any> SimpleNavigation(
    currentScreen: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    // create SaveableStateHolder.
    val saveableStateHolder = rememberSaveableStateHolder()
    Crossfade(currentScreen, modifier) {
        // Wrap the content representing the `currentScreen` inside `SaveableStateProvider`.
        // Here you can also add a screen switch animation like Crossfade where during the
        // animation multiple screens will be displayed at the same time.
        saveableStateHolder.SaveableStateProvider(it) {
            content(it)
        }
    }
}