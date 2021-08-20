package com.funny.translation.translation

import com.funny.translation.bean.Consts
import com.funny.translation.trans.TranslationException
import com.funny.translation.utils.FunnyEachText
import com.funny.translation.utils.StringUtil
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class TranslationEachText(sourceString: String?, sourceLanguage: Short, targetLanguage: Short) :
    BasicTranslationTask(
        sourceString!!, sourceLanguage, targetLanguage
    ) {
    @Throws(TranslationException::class)
    override fun getBasicText(url: String): String {
        return sourceString
    }

    @Throws(TranslationException::class)
    override fun getFormattedResult(basicText: String) {
        val chinese = StringUtil.extraChinese(basicText)
        val words: JSONObject?
        try {
            words = FunnyEachText.getWords()
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
    override val engineKind: Short
        get() = Consts.ENGINE_EACH_TEXT
}