package com.funny.translation.translate.task


import com.funny.translation.translate.TranslationEngine
import com.funny.translation.translate.engine.TextTranslationEngines

class TextTranslationTencent :
    ServerTextTranslationTask(), TranslationEngine by TextTranslationEngines.Tencent {
    companion object{
        var TAG = "TencentTranslation"
    }

    override val engineCodeName = "tencent"
}