package com.funny.translation.translate.task

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
        private const val TAG = "TranslationYouDaoNormal"
    }

    @Throws(TranslationException::class)
    override fun getBasicText(url: String): String {
        return try {
            // 发送请求参数
            val from = languageMapping[sourceLanguage]
            val to = languageMapping[targetLanguage]
            val ts = System.currentTimeMillis().toString()
            val salt = ts + (Math.random() * 9 + 1).roundToInt().toString()
            val bv = ("5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36 Edg/92.0.902.73").md5
            val sign =
                ("fanyideskweb${sourceString}${salt}Y2FYu%TNSbMCxc3t2u^XT").md5 //2021.08.18
            
            val headers = HashMap<String,String>().apply {
                put("X-Requested-With", "XMLHttpRequest")
                put("Cookie", "OUTFOX_SEARCH_USER_ID=-207808668@10.108.160.105; JSESSIONID=aaafRgWf7GPWd8W8z4yTx; OUTFOX_SEARCH_USER_ID_NCOO=1113380300.3666594; ___rl__test__cookies=$ts")
                put("Referer", "http://fanyi.youdao.com/")
                put("Origin","http://fanyi.youdao.com")
                put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36 Edg/92.0.902.73")
            }

            val params = HashMap<String,String>().apply {
                put("i",sourceString) //改回不加修改的就可以了？？？？！！！
                put("from",from!!)
                put("to",to!!)
                put("smartresult","dict")
                put("client","fanyideskweb")
                put("salt",salt)
                put("sign",sign)
                put("lts",ts)
                put("bv",bv)
                put("doctype","json")
                put("version","2.1")
                put("keyfrom","fanyi.web")
                put("action","FY_BY_CLICKBUTTON")
            }

            val html = OkHttpUtils.postForm(url,params,headers)
            //Log.d(TAG, "getBasicText: $html")
            html
        } catch (e: Exception) {
            println("发送 POST 请求出现异常！$e")
            e.printStackTrace()
            throw TranslationException(Consts.ERROR_POST)
        }

    }

    @Throws(TranslationException::class)
    override fun getFormattedResult(basicText: String) {
        try {
            val sb = StringBuilder()
            val all = JSONObject(basicText)
            if (all.has("errorCode") && all.getInt("errorCode") > 0) { //出错
                when (all.getInt("errorCode")) {
                    30, 40 -> throw TranslationException(Consts.ERROR_UNSUPPORT_LANGUAGE)
                    50 -> throw TranslationException(Consts.ERROR_DATED_ENGINE)
                    else -> throw TranslationException(Consts.ERROR_UNKNOWN)
                }
            }
            val translationResult = all.getJSONArray("translateResult")
            for (i in 0 until translationResult.length()) {
                val eachResult = translationResult.getJSONArray(i)
                for (j in 0 until eachResult.length()) {
                    val resultObject = eachResult.getJSONObject(j)
                    val resultString = resultObject.getString("tgt")
                    sb.append(resultString)
                    sb.append("\n")
                }
            }
            sb.deleteCharAt(sb.length - 1)

            println(result)
            result.setBasicResult(sb.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
            throw TranslationException(Consts.ERROR_JSON)
        } catch (e: TranslationException) {
            e.printStackTrace()
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            throw TranslationException(Consts.ERROR_UNKNOWN)
        }
    }

    override fun madeURL(): String {
        return "https://fanyi.youdao.com/translate_o?smartresult=dict&smartresult=rule"
    }

    override val isOffline: Boolean
        get() = false
}