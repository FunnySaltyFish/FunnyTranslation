package com.funny.translation.translate.ui.screen

import androidx.compose.ui.graphics.vector.ImageVector
import com.funny.translation.translate.R

class NavigationIcon(
    private val imageVector: ImageVector ?= null,
    val resourceId : Int? = null
){
    fun get() = imageVector ?: resourceId
}

sealed class TranslateScreen(val icon : NavigationIcon, val titleId : Int, val route : String) {
    object MainScreen : TranslateScreen(NavigationIcon(resourceId = R.drawable.ic_translate), R.string.nav_main, "nav_trans_main")
    object SettingScreen : TranslateScreen(NavigationIcon(resourceId = R.drawable.ic_settings), R.string.nav_settings, "nav_trans_settings")
    object PluginScreen : TranslateScreen(NavigationIcon(resourceId = R.drawable.ic_plugin), R.string.nav_plugin, "nav_trans_plugin")
    object ThanksScreen : TranslateScreen(NavigationIcon(resourceId = R.drawable.ic_thanks), R.string.nav_thanks, "nav_thanks")
    object AboutScreen : TranslateScreen(NavigationIcon(),R.string.about,"nav_trans_setting")
    object SortResultScreen : TranslateScreen(NavigationIcon(),R.string.sort_result,"nav_trans_sort_result")
    object SelectLanguageScreen : TranslateScreen(NavigationIcon(),R.string.select_language,"nav_trans_select_language")
    object UserProfileScreen : TranslateScreen(NavigationIcon(),R.string.user_profile,"nav_trans_user_profile")
}