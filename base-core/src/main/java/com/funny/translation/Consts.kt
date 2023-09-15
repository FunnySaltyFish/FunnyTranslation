package com.funny.translation

import com.funny.translation.core.R
import com.funny.translation.helper.string

object Consts {



    const val INTENT_ACTION_CLICK_FLOAT_WINDOW_TILE = "action_click_float_window_tile"
    const val INTENT_EXTRA_OPEN_FLOAT_WINDOW  = "extra_open_float_window"
    const val EXTRA_OPEN_IN_APP = "extra_open_in_app"

    const val KEY_CRASH_MSG = "crash_message"
    const val KEY_SHOW_HISTORY = "show_history"
    const val KEY_EMAIL = "email"
    const val KEY_USER_UID = "uid"
    const val KEY_ENTER_TO_TRANSLATE = "enter_to_trans"
    const val KEY_TRANS_PAGE_INPUT_BOTTOM = "trans_page_input_bottom"
    const val KEY_USER_INFO = "user_info"
    const val KEY_APP_CURRENT_SCREEN = "key_app_nav_current_screen"
    const val KEY_APP_LANGUAGE = "app_language"

    const val EXTRA_USER_INFO = "extra_user_info"

    //错误常量
    val ERROR_UNKNOWN = string(R.string.err_translate_unknown)
    val ERROR_JSON    = string(R.string.err_parse_json)
    val ERROR_IO      = string(R.string.err_io)
    const val ERROR_ILLEGAL_DATA = "数据不合法！"
    const val ERROR_ONLY_CHINESE_SUPPORT = "当前翻译模式仅支持中文！"
    val ERROR_NO_BV_OR_AV = "未检测到有效的Bv号或Av号"

    const val MODE_EACH_LINE = 1

    // 百度翻译常量
    // 为避免不必要的麻烦，开源部分不包含此部分，请您谅解
    // 您可以访问 https://api.fanyi.baidu.com/ 免费注册该项服务
    private val DEFAULT_BAIDU_APP_ID: String = ""
    private val DEFAULT_BAIDU_SECURITY_KEY: String = ""
    const val DEFAULT_BAIDU_SLEEP_TIME = 1000L

    var BAIDU_APP_ID = DEFAULT_BAIDU_APP_ID
    var BAIDU_SECURITY_KEY = DEFAULT_BAIDU_SECURITY_KEY
    var BAIDU_SLEEP_TIME = DEFAULT_BAIDU_SLEEP_TIME

    const val KEY_SOURCE_LANGUAGE = "sourceLanguage"
    const val KEY_TARGET_LANGUAGE = "targetLanguage"
    const val KEY_APP_CHANNEL = "app_channel"

    const val KEY_HIDE_NAVIGATION_BAR = "hide_nav_bar"
    const val KEY_HIDE_STATUS_BAR = "hide_status_bar"
    const val KEY_SHOW_FLOAT_WINDOW = "show_float_window"
    const val KEY_SPRING_THEME: String = "spring_theme"
    const val KEY_SORT_RESULT = "sort_result"
    const val KEY_CUSTOM_NAVIGATION = "custom_nav"
    const val KEY_FIRST_OPEN_APP = "first_open_app_v2"

    val MAX_SELECT_ENGINES get() = if (AppConfig.isVip()) 8 else 5
}