package com.funny.translation.translation

import android.net.Uri
import com.funny.translation.bean.Consts
import com.funny.translation.trans.TranslationException
import com.funny.translation.trans.TranslationResult
import org.json.JSONArray
import org.json.JSONException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.URL

class TranslationGoogleNormal(sourceString: String?, sourceLanguage: Short, targetLanguage: Short) :
    BasicTranslationTask(
        sourceString!!, sourceLanguage, targetLanguage
    ) {
    @Throws(TranslationException::class)
    override fun getBasicText(url: String): String {
        var out: PrintWriter? = null
        var `in`: BufferedReader? = null
        var result = ""
        try {
            val engineKind = engineKind
            val from = Consts.LANGUAGES[sourceLanguage.toInt()][engineKind.toInt()]
            val to = Consts.LANGUAGES[targetLanguage.toInt()][engineKind.toInt()]
            val realUrl = URL(
                String.format(
                    "https://translate.google.cn/translate_a/single?client=webapp&sl=%s&tl=%s&hl=%s&dt=at&dt=bd&dt=ex&dt=ld&dt=md&dt=qca&dt=rw&dt=rm&dt=ss&dt=t&source=btn&ssel=5&tsel=5&kc=0&tk=%s&q=%s",
                    from,
                    to,
                    to,
                    FunnyGoogleApi.tk(sourceString, "439500.3343569631"),
                    Uri.encode(sourceString)
                )
            )
            // 打开和URL之间的连接
            val conn = realUrl.openConnection()
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*")
            conn.setRequestProperty("connection", "Keep-Alive")
            conn.setRequestProperty(
                "user-agent",
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)"
            )

            // 发送POST请求必须设置如下两行
            conn.doOutput = true
            conn.doInput = true
            //1.获取URLConnection对象对应的输出流
            out = PrintWriter(conn.getOutputStream())
            //2.中文有乱码的需要将PrintWriter改为如下
            //out=new OutputStreamWriter(conn.getOutputStream(),"UTF-8")
            // 发送请求参数
            val param = ""
            out.print(param)
            // flush输出流的缓冲
            out.flush()
            // 定义BufferedReader输入流来读取URL的响应
            `in` = BufferedReader(InputStreamReader(conn.getInputStream()))
            var line: String
            while (`in`.readLine().also { line = it } != null) {
                result += line
            }
            //System.out.println("result:"+result);
        } catch (e: Exception) {
            println("发送 POST 请求出现异常！$e")
            e.printStackTrace()
            throw TranslationException(Consts.ERROR_POST)
        } //使用finally块来关闭输出流、输入流
        finally {
            try {
                out?.close()
                `in`?.close()
            } catch (ex: IOException) {
                ex.printStackTrace()
                throw TranslationException(Consts.ERROR_IO)
            }
        }
        //result=formatResult(result);
        //System.out.println(result);
        //System.out.println("post推送结果："+result);
        return result
    }

    @Throws(TranslationException::class)
    override fun getFormattedResult(basicText: String): TranslationResult {
        val result = TranslationResult(Consts.ENGINE_GOOGLE)
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
        return result
    }

    override fun madeURL(): String {
        return ""
    }

    override val isOffline: Boolean
        get() = false
    override val engineKind: Short
        get() = Consts.ENGINE_GOOGLE

    companion object {
        fun showArray(arr: Array<Array<String?>>) {
            for (arr1 in arr) {
                for (str in arr1) {
                    print(str)
                    print(" ")
                }
                println("")
            }
        }
    }
}