package com.funny.translation

import android.annotation.SuppressLint
import android.provider.Settings
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.funny.translation.bean.UserBean
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.translate.Language

object AppConfig {
    var SCREEN_WIDTH = 0
    var SCREEN_HEIGHT = 0

    var userInfo = mutableDataSaverStateOf(DataSaverUtils, Consts.KEY_USER_INFO, UserBean())
    val uid by derivedStateOf { userInfo.value.uid }
    val jwtToken by derivedStateOf { userInfo.value.jwt_token }

    var versionCode = BaseApplication.getLocalPackageInfo()?.versionCode ?: 0
    @SuppressLint("HardwareIds")
    val androidId = Settings.Secure.getString(BaseApplication.ctx.contentResolver, Settings.Secure.ANDROID_ID)

    val lowerThanM = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M

    // 下面为可设置的状态
    val sUseNewNavigation = mutableDataSaverStateOf(DataSaverUtils, Consts.KEY_CUSTOM_NAVIGATION, true)
    val sTransPageInputBottom = mutableDataSaverStateOf(DataSaverUtils, Consts.KEY_TRANS_PAGE_INPUT_BOTTOM, false)
    val sShowTransHistory = mutableDataSaverStateOf(DataSaverUtils, Consts.KEY_SHOW_HISTORY, false)
    val sTextMenuFloatingWindow = mutableDataSaverStateOf(DataSaverUtils, "KEY_TEXT_MENU_FLOATING_WINDOW", false)
    val sSpringFestivalTheme = mutableDataSaverStateOf(DataSaverUtils, Consts.KEY_SPRING_THEME, false)
    val sEnterToTranslate = mutableDataSaverStateOf(DataSaverUtils, Consts.KEY_ENTER_TO_TRANSLATE, false)
    val sHideStatusBar = mutableDataSaverStateOf(DataSaverUtils, Consts.KEY_HIDE_STATUS_BAR, true)
    val sHideBottomNav = mutableDataSaverStateOf(DataSaverUtils, Consts.KEY_CRASH_MSG, false)
    val sAutoFocus = mutableDataSaverStateOf(DataSaverUtils, "KEY_AUTO_FOCUS", true)

    fun updateJwtToken(newToken: String) {
        userInfo.value = userInfo.value.copy(jwt_token = newToken)
    }
}

object TranslateConfig {
    var sourceLanguage : Language = Language.AUTO
    var targetLanguage : Language = Language.ENGLISH
    var sourceString : String = ""
}