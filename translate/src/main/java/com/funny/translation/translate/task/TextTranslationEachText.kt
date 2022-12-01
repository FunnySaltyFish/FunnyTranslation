package com.funny.translation.translate.task

import com.funny.translation.translate.TranslationEngine
import com.funny.translation.translate.TranslationException
import com.funny.translation.Consts
import com.funny.translation.translate.engine.TextTranslationEngines
import com.funny.translation.translate.utils.FunnyEachText
import com.funny.translation.translate.utils.StringUtil
import org.json.JSONException
import java.io.IOException

class TextTranslationEachText() :
    BasicTextTranslationTask(), TranslationEngine by TextTranslationEngines.EachText{

    @Throws(TranslationException::class)
    override fun getBasicText(url: String): String {
        return sourceString
    }

    @Throws(TranslationException::class)
    override fun getFormattedResult(basicText: String) {
        val chinese = StringUtil.extraChinese(basicText)
        try {
            val words = FunnyEachText.words
            val stringBuilder = StringBuilder()
            for (element in chinese) {
                val each = element.toString()
                if (words.has(each)) {
                    stringBuilder.append(words.getString(each))
                }
                stringBuilder.append(" ")
            }
            result.setBasicResult(stringBuilder.toString())
        } catch (e: IOException) {
            e.printStackTrace()
            throw TranslationException(Consts.ERROR_IO)
        } catch (e: JSONException) {
            e.printStackTrace()
            throw TranslationException(Consts.ERROR_JSON)
        }
    }

    override fun madeURL(): String {
        return ""
    }

    override val isOffline: Boolean
        get() = true
}