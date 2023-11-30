package com.funny.translation.translate.ui

import com.funny.translation.translate.R

sealed class TranslateScreen(val titleId: Int, val route: String) {
    object MainScreen : TranslateScreen(R.string.nav_main, "nav_trans_main")
    object ImageTranslateScreen: TranslateScreen(R.string.image_translate, "nav_trans_img")
    object FavoriteScreen: TranslateScreen(R.string.favorite, "nav_trans_favorite")
    object SettingScreen : TranslateScreen(R.string.nav_settings, "nav_trans_settings")
    object PluginScreen : TranslateScreen(R.string.nav_plugin, "nav_trans_plugin")
    object ThanksScreen : TranslateScreen(R.string.nav_thanks, "nav_thanks")
    object AboutScreen : TranslateScreen(R.string.about, "nav_trans_about")
    object OpenSourceLibScreen: TranslateScreen(R.string.open_source_library, "nav_trans_open_source_lib")
    object SortResultScreen :     TranslateScreen(R.string.sort_result, "nav_trans_sort_result")
    object SelectLanguageScreen : TranslateScreen(R.string.select_language, "nav_trans_select_language")
    object UserProfileScreen :    TranslateScreen(R.string.user_profile, "nav_trans_user_profile")
    object TransProScreen:    TranslateScreen(R.string.trans_pro, "nav_trans_pro")
    object ThemeScreen : TranslateScreen(R.string.theme, "nav_trans_theme")
    object FloatWindowScreen: TranslateScreen(R.string.float_window, "nav_trans_float_window_screen")
    object AppRecommendationScreen: TranslateScreen(R.string.recommendation_app, "nav_app_recommendation")
    object LongTextTransScreen: TranslateScreen(R.string.long_text_trans, "nav_long_text_trans")

    object LongTextTransListScreen: TranslateScreen(R.string.long_text_trans, "nav_long_text_trans_list")
    object LongTextTransDetailScreen: TranslateScreen(R.string.long_text_trans, "nav_long_text_trans_detail/{id}")
    object TextEditorScreen: TranslateScreen(R.string.edit_text, "nav_text_editor?action={action}")
    object DraftScreen: TranslateScreen(R.string.drafts, "nav_drafts")
    object ChatScreen: TranslateScreen(R.string.chat, "nav_chat")
    object BuyAIPointScreen: TranslateScreen(R.string.buy_ai_point, "nav_buy_ai_point?planName={planName}")
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