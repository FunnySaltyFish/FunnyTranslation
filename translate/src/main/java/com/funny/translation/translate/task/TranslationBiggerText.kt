package com.funny.translation.translate.task

import android.preference.PreferenceManager
import com.funny.translation.trans.Language
import com.funny.translation.trans.TranslationException
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.utils.FunnyBiggerText

class TranslationBiggerText(sourceString: String?, sourceLanguage: Language, targetLanguage: Language) :
    BasicTranslationTask(
        sourceString!!, sourceLanguage, targetLanguage
    ) {

    override val languageMapping: Map<Language, String>
        get() = TODO("Not yet implemented")

    override val name: String
        get() = TODO("Not yet implemented")

    @Throws(TranslationException::class)
    override fun getBasicText(url: String): String {
        return sourceString
    }

    @Throws(TranslationException::class)
    override fun getFormattedResult(basicText: String) {
        val performance =
            PreferenceManager.getDefaultSharedPreferences(FunnyApplication.ctx)
                .getString("preference_bigger_text_performance", "1")!!.toInt()
        FunnyBiggerText.fillChar =
            PreferenceManager.getDefaultSharedPreferences(FunnyApplication.ctx)
                .getString("preference_bigger_text_fill_char", "")
        val str = when (performance) {
            0 -> FunnyBiggerText.drawWideString(FunnyApplication.ctx, basicText)
            1 -> FunnyBiggerText.drawMiddleString(FunnyApplication.ctx, basicText)
            2 -> FunnyBiggerText.drawNarrowString(FunnyApplication.ctx, basicText)
            else -> basicText
        }
        result.setBasicResult(str)
    }

    override fun madeURL(): String {
        return ""
    }

    override val isOffline: Boolean
        get() = true
}