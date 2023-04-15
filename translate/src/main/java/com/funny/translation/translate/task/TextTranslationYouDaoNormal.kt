package com.funny.translation.translate.task

import com.funny.translation.network.OkHttpUtils
import com.funny.translation.network.ServiceCreator.BASE_URL
import com.funny.translation.translate.TranslationEngine
import com.funny.translation.translate.TranslationException
import com.funny.translation.translate.engine.TextTranslationEngines
import org.json.JSONObject

class TextTranslationYouDaoNormal :
    BasicTextTranslationTask(), TranslationEngine by TextTranslationEngines.Youdao{
    companion object{
        var TAG = "YoudaoTranslation"
    }

    @Throws(TranslationException::class)
    override fun getBasicText(url: String): String {
        val from = languageMapping[sourceLanguage] ?: "auto"
        val to = languageMapping[targetLanguage] ?: "zh"
        val headersMap = hashMapOf(
            "Referer" to "FunnyTranslation"
        )
        val params = hashMapOf(
            "source" to from,
            "target" to to,
            "text" to sourceString,
            "engine" to "youdao"
        )
        return OkHttpUtils.get(url, headersMap, params)
    }

    @Throws(TranslationException::class)
    override fun getFormattedResult(basicText: String) {
        val obj = JSONObject(basicText)
        if (obj.getInt("code")==50){
            result.setBasicResult(obj.getString("translation"))
            result.detailText = obj.getString("detail")
        }else{
            result.setBasicResult(obj.getString("error_msg"))
        }
    }

    override fun madeURL(): String {
        return "$BASE_URL/api/translate"
    }

    override val isOffline: Boolean
        get() = false
}