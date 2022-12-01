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
        val from = languageMapping[sourceLanguage]
        val to = languageMapping[targetLanguage]
        val headersMap = hashMapOf(
            "Referer" to "FunnyTranslation"
        )
        val apiUrl = "$url?text=$sourceString&engine=youdao&source=$from&target=$to"
        //        Log.i(TAG, "youdao api获取到的基本result是$transResult");
        return OkHttpUtils.get(apiUrl, headersMap)
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