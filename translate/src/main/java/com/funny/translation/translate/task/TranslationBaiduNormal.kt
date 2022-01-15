package com.funny.translation.translate.task

import android.util.Log
import com.funny.translation.network.OkHttpUtils
import com.funny.translation.trans.TranslationEngine
import com.funny.translation.trans.TranslationException
import com.funny.translation.translate.engine.TranslationEngines
import org.json.JSONObject

class TranslationBaiduNormal :
    BasicTranslationTask(), TranslationEngine by TranslationEngines.BaiduNormal {
    companion object{
        var TAG = "BaiduTranslation"
    }

    @Throws(TranslationException::class)
    override fun getBasicText(url: String): String {
        val from = languageMapping[sourceLanguage]
        val to = languageMapping[targetLanguage]
        val headersMap = hashMapOf(
            "Referer" to "FunnyTranslation"
        )
        val apiUrl = "$url?text=$sourceString&engine=baidu&source=$from&target=$to"
        val transResult = OkHttpUtils.get(apiUrl, headersMap)
        Log.i(TAG, "baidu api获取到的基本result是$transResult");
        return transResult
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
        return "https://api.funnysaltyfish.fun/trans/v1/api/translate"
    }

    override val isOffline: Boolean
        get() = false
}