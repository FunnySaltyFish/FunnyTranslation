package com.funny.translation.translate.task

import androidx.preference.PreferenceManager
import com.funny.translation.FunnyApplication
import com.funny.translation.bean.Consts
import com.funny.translation.trans.TranslationException
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
    override fun getFormattedResult(basicText: String) {
        val performance =
            PreferenceManager.getDefaultSharedPreferences(FunnyApplication.getFunnyContext())
                .getString("preference_bigger_text_performance", "1")!!.toInt()
        FunnyBiggerText.fillChar =
            PreferenceManager.getDefaultSharedPreferences(FunnyApplication.getFunnyContext())
                .getString("preference_bigger_text_fill_char", "")
        val str = when (performance) {
            0 -> FunnyBiggerText.drawWideString(FunnyApplication.getFunnyContext(), basicText)
            1 -> FunnyBiggerText.drawMiddleString(FunnyApplication.getFunnyContext(), basicText)
            2 -> FunnyBiggerText.drawNarrowString(FunnyApplication.getFunnyContext(), basicText)
            else -> basicText
        }
        result.setBasicResult(str)
    }

    override fun madeURL(): String {
        return ""
    }

    override val isOffline: Boolean
        get() = true
    override val engineName: String
        get() = Consts.ENGINE_BIGGER_TEXT
}