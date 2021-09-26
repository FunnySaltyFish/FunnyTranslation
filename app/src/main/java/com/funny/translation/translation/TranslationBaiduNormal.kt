package com.funny.translation.translation

import com.funny.translation.bean.Consts
import com.funny.translation.trans.TranslationException
import com.funny.translation.trans.TranslationResult
import com.funny.translation.utils.StringUtil
import org.json.JSONException
import org.json.JSONObject

class TranslationBaiduNormal(sourceString: String, sourceLanguage: Short, targetLanguage: Short) :
    BasicTranslationTask(
        sourceString, sourceLanguage, targetLanguage
    ) {
    var TAG = "BaiduTranslation"
    @Throws(TranslationException::class)
    override fun getBasicText(url: String): String {
        //Log.i(TAG,String.format("正在使用百度翻译！用的appid是%s",Consts.BAIDU_APP_ID));
        val engineKind = engineName
        val api = BaiduTransApi.getBaiduTransApi(Consts.BAIDU_APP_ID, Consts.BAIDU_SECURITY_KEY)
        val from = Consts.LANGUAGES[sourceLanguage.toInt()][engineKind.toInt()]
        val to = Consts.LANGUAGES[targetLanguage.toInt()][engineKind.toInt()]
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

    override val engineName: String
        get() = Consts.ENGINE_BAIDU_NORMAL
}