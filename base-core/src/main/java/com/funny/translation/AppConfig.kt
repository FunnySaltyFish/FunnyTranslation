package com.funny.translation

import com.funny.translation.trans.Language

object AppConfig {
    var SCREEN_WIDTH = 0
    var SCREEN_HEIGHT = 0

    var uid = 0L
    var versionCode = BaseApplication.getLocalPackageInfo()?.versionCode ?: 0
}

object TranslateConfig {
    var sourceLanguage : Language = Language.AUTO
    var targetLanguage : Language = Language.ENGLISH
    var sourceString : String = ""
}