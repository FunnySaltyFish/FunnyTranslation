package com.funny.translation.translate.task

import com.funny.translation.translate.TranslationEngine
import com.funny.translation.translate.engine.TextTranslationEngines

class TextTranslationYouDaoNormal :
    ServerTextTranslationTask(), TranslationEngine by TextTranslationEngines.Youdao{
    companion object{
        var TAG = "YoudaoTranslation"
    }

    override val engineCodeName = "youdao"
}