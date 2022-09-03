package com.funny.translation.translate.task

import android.net.Uri
import com.funny.translation.network.OkHttpUtils
import com.funny.translation.translate.TranslationEngine
import com.funny.translation.translate.TranslationException
import com.funny.translation.Consts
import com.funny.translation.translate.engine.TranslationEngines
import org.json.JSONArray
import org.json.JSONException

class TranslationGoogleV2() :
    BasicTranslationTask(), TranslationEngine by TranslationEngines.GoogleNormal{

    companion object{
        private const val TAG = "TransGoogle"
    }

    @Throws(TranslationException::class)
    override fun getBasicText(url: String): String {
        return try {
            val from = languageMapping[sourceLanguage]
            val to = languageMapping[targetLanguage]
            val realUrl = String.format(
                "https://translate.google.cn/translate_a/single?client=webapp&sl=%s&tl=%s&hl=%s&dt=at&dt=bd&dt=ex&dt=ld&dt=md&dt=qca&dt=rw&dt=rm&dt=ss&dt=t&source=btn&ssel=5&tsel=5&kc=0&tk=%s&q=%s",
                from,
                to,
                to,
                FunnyGoogleApi.tk(sourceString, "439500.3343569631"),
                Uri.encode(sourceString)
            )
            val headers = HashMap<String,String>().apply {
                put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36 Edg/92.0.902.73")
            }
            val html = OkHttpUtils.get(realUrl,headers)
            //Log.d(TAG, "getBasicText: $html")
            html
        } catch (e: Exception) {
            println("发送 POST 请求出现异常！$e")
            e.printStackTrace()
            throw TranslationException(Consts.ERROR_POST)
        } //使用finally块来关闭输出流、输入流
    }

    @Throws(TranslationException::class)
    override fun getFormattedResult(basicText: String) {
        try {
            val all = JSONArray(basicText)
            val sb = StringBuilder()
            var i = 0
            val jsonArray_0 = all.getJSONArray(0)
            val jsonArray_0_length = jsonArray_0.length()
            while (i < jsonArray_0_length && !jsonArray_0.getJSONArray(i).isNull(0)) {
                val string = jsonArray_0.getJSONArray(i).getString(0)
                if (string != "null") {
                    sb.append(string)
                    i++
                }
            }
            val basicResult = sb.toString()
            result.setBasicResult(basicResult)
            //System.out.println(basicResult);
//            if (!all.isNull(5)) {
//                val detailArr = all.getJSONArray(5)
//                val length = detailArr.length()
//                val detailTexts: Array<Array<String?>?> = arrayOfNulls(length)
//                for (j in 0 until length) {
//                    val eachDetail = detailArr.getJSONArray(j) //5 j
//                    if (eachDetail is JSONArray) {
//                        val text = eachDetail.getString(0)
//                        if (eachDetail.isNull(2)) {
//                            detailTexts[j] = arrayOf(eachDetail.getString(0))
//                            continue
//                        }
//                        val explanation = eachDetail.getJSONArray(2) //5 j 2
//                        detailTexts[j] = arrayOfNulls(explanation.length() + 1) //多一个，第一个放文字
//                        detailTexts[j][0] = text
//                        for (k in 0 until explanation.length()) {
//                            detailTexts[j][k + 1] =
//                                explanation.getJSONArray(k).getString(0) // 5 j 2 k 0
//                        }
//                    }
//                }
//                //System.out.println("current detailText length : "+detailTexts.length);
//                showArray(detailTexts)
//            }
            //System.out.println(all);
        } catch (e: JSONException) {
            e.printStackTrace()
            throw TranslationException(Consts.ERROR_JSON)
        }
    }

    private fun getRpc(){
        //SCRIPT_ENGINE.eval()
    }

//    function get_rpc(query_text,from,to){
//        let param = JSON.stringify([[query_text, from, to, true], [1]]);
//        let rpc=JSON.stringify([[["MkEWBc", param, null, "generic"]]]);
//        return rpc;//{"f.req":rpc};
//    }

    override fun madeURL(): String {
        return "https://translate.google.cn/_/TranslateWebserverUi/data/batchexecute"
    }

    override val isOffline: Boolean
        get() = false
}