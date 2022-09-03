package com.funny.translation

import android.annotation.SuppressLint
import android.provider.Settings
import com.funny.translation.translate.Language

object AppConfig {
    var SCREEN_WIDTH = 0
    var SCREEN_HEIGHT = 0

    var uid = 0L
    var versionCode = BaseApplication.getLocalPackageInfo()?.versionCode ?: 0
    @SuppressLint("HardwareIds")
    val androidId = Settings.Secure.getString(BaseApplication.ctx.contentResolver, Settings.Secure.ANDROID_ID)

    val lowerThanM = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M
}

object TranslateConfig {
    var sourceLanguage : Language = Language.AUTO
    var targetLanguage : Language = Language.ENGLISH
    var sourceString : String = ""
}