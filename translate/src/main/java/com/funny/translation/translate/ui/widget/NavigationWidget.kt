package com.funny.translation.translate.ui.widget

import android.util.Log
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.funny.bottomnavigation.FunnyBottomNavigation
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.translation.translate.bean.Consts
import com.funny.translation.translate.ui.screen.TranslateScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "CustomNavigation"

@ExperimentalAnimationApi
@Composable
fun CustomNavigationItem(
    item: TranslateScreen,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val background =
        if (isSelected) MaterialTheme.colors.surface.copy(alpha = 0.2f) else Color.Transparent
    val contentColor =
        if (isSelected) MaterialTheme.colors.onSurface.copy(1.0f) else MaterialTheme.colors.onBackground
    Box(
        Modifier
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            val icon = item.icon.get()
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
    backgroundColor: Color = MaterialTheme.colors.primarySurface,
    contentColor: Color = contentColorFor(backgroundColor),
    elevation: Dp = BottomNavigationDefaults.Elevation,
    screens: Array<TranslateScreen>,
    currentScreen: TranslateScreen = screens[0],
    onItemClick: (TranslateScreen) -> Unit
) {
    val customNav by rememberDataSaverState(key = Consts.KEY_CUSTOM_NAVIGATION, default = true)
    if (!customNav) {
        Surface(
            color = backgroundColor,
            contentColor = contentColor,
            elevation = elevation,
            modifier = modifier,
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
        val highlightColor = MaterialTheme.colors.primary.toArgb()
        var firstInit by remember {
            mutableStateOf(true)
        }
        val scope = rememberCoroutineScope()

        Surface(
            color = backgroundColor,
            contentColor = contentColor,
            elevation = elevation,
            modifier = modifier,
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
                        navigationBgColor = (backgroundColor.toArgb())
                        normalColor = contentColor.toArgb()
                        clickMargin = 24 // 拓宽点击边界
                        animationDuration = 500
                        initIconButtons(screens.map { screen -> screen.icon.resourceId!! }.toIntArray())
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            height
                        )
                        setOnItemClickListener { position ->
                            onItemClick(screens[position])
                        }
                        scope.launch {
                            delay(300)
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