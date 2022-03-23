package com.funny.translation.translate.task

import android.util.Log
import com.funny.translation.network.OkHttpUtils
import com.funny.translation.trans.Language
import com.funny.translation.trans.TranslationEngine
import com.funny.translation.trans.TranslationException
import com.funny.translation.translate.bean.Consts
import com.funny.translation.translate.engine.TranslationEngines
import com.funny.translation.translate.extentions.md5
import org.json.JSONException
import org.json.JSONObject
import kotlin.math.roundToInt

class TranslationYouDaoNormal() :
    BasicTranslationTask(), TranslationEngine by TranslationEngines.Youdao{
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
        val transResult = OkHttpUtils.get(apiUrl, headersMap)
//        Log.i(TAG, "youdao api获取到的基本result是$transResult");
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