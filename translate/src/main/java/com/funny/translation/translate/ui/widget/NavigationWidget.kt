package com.funny.translation.translate.ui.widget

import android.util.Log
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.funny.bottomnavigation.FunnyBottomNavigation
import com.funny.translation.AppConfig
import com.funny.translation.translate.R
import com.funny.translation.translate.ui.screen.TranslateScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

@ExperimentalAnimationApi
@Composable
fun CustomNavigationItem(
    item: TranslateScreen,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val background =
        if (isSelected) MaterialTheme.colorScheme.surface.copy(alpha = 0.2f) else Color.Transparent
    val contentColor =
        if (isSelected) MaterialTheme.colorScheme.onSurface.copy(1.0f) else MaterialTheme.colorScheme.onBackground
    Box(
        Modifier
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            val icon = item.icon?.get()
            if (icon is ImageVector) {
                Icon(imageVector = icon, contentDescription = "", tint = contentColor)
            } else if (icon is Int) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = "",
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(id = item.titleId), color = contentColor)
            }
        }
    }
}

val BottomNavigationHeight = 56.dp

@ExperimentalAnimationApi
@Composable
fun CustomNavigation(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = contentColorFor(backgroundColor),
    elevation: Dp = 8.dp,
    screens: Array<TranslateScreen>,
    currentScreen: TranslateScreen = screens[0],
    onItemClick: (TranslateScreen) -> Unit
) {
    val customNav by AppConfig.sUseNewNavigation
    if (!customNav) {
        Surface(
            color = backgroundColor,
            contentColor = contentColor,
            modifier = modifier,
            tonalElevation = elevation
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding)
                    .height(BottomNavigationHeight),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                screens.forEach { screen ->
                    CustomNavigationItem(item = screen, isSelected = currentScreen == screen) {
                        onItemClick(screen)
                    }
                }
            }
        }
    } else {
        val height = with(LocalDensity.current) { BottomNavigationHeight.toPx().toInt() }
        val width = with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.toPx().toInt() }
        val highlightColor = MaterialTheme.colorScheme.primary.toArgb()
        var firstInit by remember {
            mutableStateOf(true)
        }
        val scope = rememberCoroutineScope()

        Surface(
            color = backgroundColor,
            contentColor = contentColor,
            modifier = modifier,
            tonalElevation = elevation
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding)
                    .height(BottomNavigationHeight),

                factory = {
                    FunnyBottomNavigation(it, null).apply {
                        imageWidth = height * 2 / 5
                        imageHeight = height * 2 / 5
                        this.highlightColor = highlightColor
                        navigationBgColor = backgroundColor.toArgb()
                        normalColor = contentColor.toArgb()
                        clickMargin = width / 8 - imageWidth / 2 // 拓宽点击边界
                        animationDuration = 500
                        initIconButtons(screens.map { screen -> screen.icon?.resourceId ?: R.drawable.ic_thanks }.toIntArray())
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            height
                        )
                        setOnItemClickListener { position ->
                            onItemClick(screens[position])
                        }
                        scope.launch {
                            while (!hasInitialized()){
                                delay(100)
                            }
                            firstInit = false
                        }
                    }
                },
                update = {
                    if (!firstInit){ // 避免未初始化时即跳转
                        Log.d(TAG, "CustomNavigation: 手动跳转到 ${currentScreen.route}")
                        it.moveTo(screens.indexOf(currentScreen), hasAnimation = true, performClick = false)
                    }
                }
            )
        }
    }
}