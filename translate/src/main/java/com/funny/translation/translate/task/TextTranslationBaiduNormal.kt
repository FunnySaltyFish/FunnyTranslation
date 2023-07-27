package com.funny.translation.translate.task

import com.funny.translation.translate.TranslationEngine
import com.funny.translation.translate.engine.TextTranslationEngines

class TextTranslationBaiduNormal :
    ServerTextTranslationTask(), TranslationEngine by TextTranslationEngines.BaiduNormal {
    companion object{
        var TAG = "BaiduTranslation"
    }

    override val engineCodeName = "baidu"

}