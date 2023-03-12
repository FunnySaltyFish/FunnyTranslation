package com.funny.translation.translate.ui.screen

import androidx.compose.ui.graphics.vector.ImageVector
import com.funny.translation.translate.R

class NavigationIcon(
    private val imageVector: ImageVector ?= null,
    val resourceId : Int? = null
){
    fun get() = imageVector ?: resourceId
}


sealed class TranslateScreen(val icon : NavigationIcon?, val titleId : Int, val route : String) {
    object MainScreen : TranslateScreen(NavigationIcon(resourceId = R.drawable.ic_translate), R.string.nav_main, "nav_trans_main")
    object SettingScreen : TranslateScreen(NavigationIcon(resourceId = R.drawable.ic_settings), R.string.nav_settings, "nav_trans_settings")
    object PluginScreen : TranslateScreen(NavigationIcon(resourceId = R.drawable.ic_plugin), R.string.nav_plugin, "nav_trans_plugin")
    object ThanksScreen : TranslateScreen(NavigationIcon(resourceId = R.drawable.ic_thanks), R.string.nav_thanks, "nav_thanks")
    object AboutScreen : TranslateScreen(null,R.string.about,"nav_trans_setting")
    object SortResultScreen :     TranslateScreen(null,R.string.sort_result,"nav_trans_sort_result")
    object SelectLanguageScreen : TranslateScreen(null,R.string.select_language,"nav_trans_select_language")
    object UserProfileScreen :    TranslateScreen(null,R.string.user_profile,"nav_trans_user_profile")
    object TransProScreen:    TranslateScreen(null, R.string.trans_pro, "nav_trans_pro")
    object ThemeScreen : TranslateScreen(null, R.string.theme, "nav_trans_theme")

    companion object {
        val Saver = { screen: TranslateScreen ->
            screen.route
        }

        val Restorer = { str: String ->
            when (str) {
                MainScreen.route -> MainScreen
                SettingScreen.route -> SettingScreen
                PluginScreen.route -> PluginScreen
                ThanksScreen.route -> ThanksScreen
                else -> MainScreen
            }
        }
    }
}
