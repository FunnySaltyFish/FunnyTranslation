package com.funny.translation.translate.ui.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.funny.translation.translate.ui.screen.TranslateScreen

@ExperimentalAnimationApi
@Composable
fun CustomNavigationItem(
    item : TranslateScreen,
    isSelected : Boolean = false,
    onClick : ()->Unit
) {
    val background = if (isSelected) MaterialTheme.colors.surface.copy(alpha = 0.2f) else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colors.onSurface.copy(1.0f) else MaterialTheme.colors.onBackground
    Box(
        Modifier
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick)
            ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically){
            val icon = item.icon.get()
            if (icon is ImageVector){
                Icon(imageVector = icon, contentDescription = "", tint = contentColor)
            }else if( icon is Int){
                Icon(painter = painterResource(id = icon), contentDescription = "", tint = contentColor, modifier = Modifier.size(20.dp))
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
    screens : Array<TranslateScreen>,
    currentScreen: TranslateScreen = screens[0],
    onItemClick: (TranslateScreen) -> Unit
) {
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
}