package com.funny.translation.translation

import androidx.preference.PreferenceManager
import com.funny.translation.FunnyApplication
import com.funny.translation.bean.Consts
import com.funny.translation.trans.Translation
import com.funny.translation.trans.TranslationException
import com.funny.translation.trans.TranslationResult
import com.funny.translation.utils.FunnyBiggerText

class TranslationBiggerText(sourceString: String?, sourceLanguage: Short, targetLanguage: Short) :
    BasicTranslationTask(
        sourceString!!, sourceLanguage, targetLanguage
    ) {
    @Throws(TranslationException::class)
    override fun getBasicText(url: String): String {
        return sourceString
    }

    @Throws(TranslationException::class)
    override fun getFormattedResult(basicText: String): TranslationResult {
        val performance =
            PreferenceManager.getDefaultSharedPreferences(FunnyApplication.getFunnyContext())
                .getString("preference_bigger_text_performance", "1")!!.toInt()
        FunnyBiggerText.fillChar =
            PreferenceManager.getDefaultSharedPreferences(FunnyApplication.getFunnyContext())
                .getString("preference_bigger_text_fill_char", "")
        var str: String? = ""
        when (performance) {
            0 -> str = FunnyBiggerText.drawWideString(FunnyApplication.getFunnyContext(), basicText)
            1 -> str =
                FunnyBiggerText.drawMiddleString(FunnyApplication.getFunnyContext(), basicText)
            2 -> str =
                FunnyBiggerText.drawNarrowString(FunnyApplication.getFunnyContext(), basicText)
        }
        return TranslationResult(engineKind, Translation(str!!), sourceString, null)
    }

    override fun madeURL(): String {
        return ""
    }

    override val isOffline: Boolean
        get() = true
    override val engineKind: Short
        get() = Consts.ENGINE_BIGGER_TEXT
}