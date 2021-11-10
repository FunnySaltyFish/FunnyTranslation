package com.funny.translation.translate.ui.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.materialIcon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.funny.translation.translate.R

class NavigationIcon(
    private val imageVector: ImageVector?= null,
    val resourceId : Int? = null
){
    fun get() = imageVector ?: resourceId
}

sealed class TranslateScreen(val icon : NavigationIcon, val titleId : Int, val route : String) {
    object MainScreen : TranslateScreen(NavigationIcon(Icons.Default.Home), R.string.nav_main, "nav_trans_main")
    object SettingScreen : TranslateScreen(NavigationIcon(Icons.Default.Settings), R.string.nav_settings, "nav_trans_settings")
    object PluginScreen : TranslateScreen(NavigationIcon(resourceId = R.drawable.ic_plugin), R.string.nav_plugin, "nav_trans_plugin")
    object ThanksScreen : TranslateScreen(NavigationIcon(resourceId = R.drawable.ic_thanks), R.string.nav_thanks, "nav_thanks")
}