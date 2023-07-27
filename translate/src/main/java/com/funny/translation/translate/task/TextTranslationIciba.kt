package com.funny.translation.translate.task

import com.funny.translation.translate.TranslationEngine
import com.funny.translation.translate.engine.TextTranslationEngines

class TextTranslationIciba() :
    ServerTextTranslationTask(), TranslationEngine by TextTranslationEngines.Jinshan {

    companion object{
        const val TAG = "TranslationJinshanEasy"
    }

    override val engineCodeName = "iciba"
}