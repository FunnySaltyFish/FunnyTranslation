package com.funny.translation.translate.task

import com.funny.translation.network.OkHttpUtils
import com.funny.translation.network.ServiceCreator
import com.funny.translation.translate.TranslationException
import org.json.JSONObject

/**
 * 通用的走服务器的翻译任务
 * @property engineCodeName String
 * @property isOffline Boolean
 */
abstract class ServerTextTranslationTask: BasicTextTranslationTask() {
    abstract val engineCodeName: String

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
            "engine" to engineCodeName
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
        return "${ServiceCreator.BASE_URL}/api/translate"
    }

    override val isOffline: Boolean
        get() = false
}