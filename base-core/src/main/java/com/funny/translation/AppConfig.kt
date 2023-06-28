package com.funny.translation

import android.annotation.SuppressLint
import android.provider.Settings
import android.util.Log
import androidx.annotation.Keep
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.funny.translation.bean.UserBean
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.theme.ThemeConfig
import com.funny.translation.theme.ThemeType
import com.funny.translation.translate.Language

private const val TAG = "AppConfig"

@SuppressLint("HardwareIds")
@Keep
object AppConfig {
    var SCREEN_WIDTH = 0
    var SCREEN_HEIGHT = 0

    var userInfo = mutableDataSaverStateOf(DataSaverUtils, Consts.KEY_USER_INFO, UserBean())
    val uid by derivedStateOf { userInfo.value.uid }
    val jwtToken by derivedStateOf { userInfo.value.jwt_token }

    var versionCode = BaseApplication.getLocalPackageInfo()?.versionCode ?: 0

    // 隐私合规，延迟获取
    val androidId by lazy {
        Log.d(TAG, "get Android_ID")
        Settings.Secure.getString(BaseApplication.ctx.contentResolver, Settings.Secure.ANDROID_ID)
    }

    val lowerThanM = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M

    // 下面为可设置的状态
    val sTextMenuFloatingWindow = mutableDataSaverStateOf(DataSaverUtils, "KEY_TEXT_MENU_FLOATING_WINDOW", false)
    val sSpringFestivalTheme = mutableDataSaverStateOf(DataSaverUtils, Consts.KEY_SPRING_THEME, false)
    val sEnterToTranslate = mutableDataSaverStateOf(DataSaverUtils, Consts.KEY_ENTER_TO_TRANSLATE, false)
    val sHideBottomNavBar = mutableDataSaverStateOf(DataSaverUtils, Consts.KEY_CRASH_MSG, false)
    val sAutoFocus = mutableDataSaverStateOf(DataSaverUtils, "KEY_AUTO_FOCUS", true)
    val sShowFloatWindow = mutableDataSaverStateOf(DataSaverUtils, Consts.KEY_SHOW_FLOAT_WINDOW, false)

    // 以下为Pro专享
    val sParallelTrans = mutableDataSaverStateOf(DataSaverUtils, "KEY_PARALLEL_TRANS", false)
    val sShowDetailResult = mutableDataSaverStateOf(DataSaverUtils, "KEY_SHOW_DETAIL_RESULT", false)

    fun updateJwtToken(newToken: String) {
        userInfo.value = userInfo.value.copy(jwt_token = newToken)
    }

    fun isVip() = userInfo.value.isValidVip()

    // 开启 VIP 的一些功能，供体验
    fun enableVipFeatures(){
        sParallelTrans.value = true
        sShowDetailResult.value = true
    }

    private fun disableVipFeatures(){
        sParallelTrans.value = false
        sShowDetailResult.value = false
        ThemeConfig.updateThemeType(ThemeType.Default)
    }

    fun logout(){
        userInfo.value = UserBean()
        disableVipFeatures()
    }

    fun login(userBean: UserBean){
        userInfo.value = userBean
        if (userBean.isValidVip()) enableVipFeatures()
        else disableVipFeatures()
    }
}

object GlobalTranslationConfig {
    var sourceLanguage : Language = Language.AUTO
    var targetLanguage : Language = Language.ENGLISH
    var sourceString : String = ""
}
