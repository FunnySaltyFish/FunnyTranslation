package com.funny.translation.translate.task

import com.funny.translation.trans.Language
import com.funny.translation.trans.TranslationException
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.R
import com.funny.translation.translate.bean.Consts
import com.funny.translation.translate.utils.StringUtil
import org.json.JSONException
import org.json.JSONObject

class TranslationBaiduNormal(sourceString: String, sourceLanguage: Language, targetLanguage: Language) :
    BasicTranslationTask(
        sourceString, sourceLanguage, targetLanguage
    ) {
    companion object{
        var TAG = "BaiduTranslation"
    }

    override val languageMapping: Map<Language, String>
        get() = mapOf(
            Language.AUTO to "auto",
            Language.CHINESE to "zh",
            Language.ENGLISH to "en",
            Language.JAPANESE to "jp",
            Language.KOREAN to "kor",
            Language.FRENCH to "fra",
            Language.RUSSIAN to "ru",
            Language.GERMANY to "de",
            Language.WENYANWEN to "wyw",
            Language.THAI to "th"
        )

    @Throws(TranslationException::class)
    override fun getBasicText(url: String): String {
        //Log.i(TAG,String.format("正在使用百度翻译！用的appid是%s",Consts.BAIDU_APP_ID));
        val api = BaiduTransApi.getBaiduTransApi(Consts.BAIDU_APP_ID, Consts.BAIDU_SECURITY_KEY)
        val from = languageMapping[sourceLanguage]
        val to = languageMapping[targetLanguage]
        //Log.i(TAG,"baidu api获取到的基本result是"+transResult);
        return api.getTransResult(sourceString, from, to)
    }

    @Throws(TranslationException::class)
    override fun getFormattedResult(basicText: String) {
        try {
            val sb = StringBuilder()
            val all = JSONObject(basicText)
            val trans_result = all.getJSONArray("trans_result")
            for (i in 0 until trans_result.length()) {
                val resultObj = trans_result.getJSONObject(i)
                var str1 = resultObj.getString("dst")
                if (StringUtil.isUnicode(str1)) {
                    str1 = StringUtil.unicodeToString(str1)
                }
                sb.append(str1)
                sb.append("\n")
            }
            sb.deleteCharAt(sb.length - 1)
            result.setBasicResult(sb.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
            throw TranslationException(Consts.ERROR_JSON)
        } catch (e: Exception) {
            e.printStackTrace()
            throw TranslationException(Consts.ERROR_UNKNOWN)
        }
    }

    override fun madeURL(): String {
        return ""
    }

    override val isOffline: Boolean
        get() = false

    override val name: String
        get() = FunnyApplication.ctx.resources.getString(R.string.engine_baidu)

    override val id: Int
        get() = 1
}