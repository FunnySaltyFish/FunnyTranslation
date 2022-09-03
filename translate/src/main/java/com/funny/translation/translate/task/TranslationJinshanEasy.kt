package com.funny.translation.translate.task

import android.util.Log
import com.funny.translation.network.OkHttpUtils
import com.funny.translation.translate.TranslationEngine
import com.funny.translation.translate.TranslationException
import com.funny.translation.Consts
import com.funny.translation.translate.engine.TranslationEngines
import com.funny.translation.helper.buildMarkdown
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.math.BigInteger
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.*
import kotlin.math.min

class TranslationJinshanEasy() :
    BasicTranslationTask(), TranslationEngine by TranslationEngines.Jinshan {

    companion object{
        const val TAG = "TranslationJinshanEasy"
    }

    @Throws(TranslationException::class)
    override fun getBasicText(url: String): String {
        return try {
            val string = OkHttpUtils.get(url) //OkHttpUtil.getWithIP(url,"182.32.234.161",9999);
            Log.i(TAG, "获取到的string:$string")
            string
        } catch (e: IOException) {
            e.printStackTrace()
            throw TranslationException(Consts.ERROR_POST)
        }
    }

    //版本 2
    @Throws(TranslationException::class)
    override fun getFormattedResult(basicText: String) {
        fun parseBaseInfo(baseInfo : JSONObject, isDetail : Boolean = false) : String {
            val symbols = baseInfo.getJSONArray("symbols").getJSONObject(0)
            val parts = symbols.getJSONArray("parts")
            val sb = StringBuilder()
            if (isDetail){
                sb.append(" **${baseInfo.getString("word_name")}**  ")
            }
            for (i in 0 until parts.length()){
                val eachPart = parts.getJSONObject(i)
                val means = eachPart.getJSONArray("means")
                if (isDetail)sb.append(" **${eachPart.getString("part")}** ")
                else sb.append(eachPart.getString("part"))
                sb.append(" ")
                for (j in 0 until means.length()) {
                    sb.append(means.getString(j))
                    sb.append("; ")
                }
                sb.append(if(isDetail)"  \n  \n" else "\n")
            }
            sb.deleteCharAt(sb.length - 1)
            return sb.toString()
        }

        try {
            val all = JSONObject(basicText)
            if (all.getInt("status") == 1) {
                val message = all.getJSONObject("message")
                val baseInfo = message.getJSONObject("baesInfo")
                if (baseInfo.has("symbols")) { //释义比较丰富的词汇
                    result.setBasicResult(parseBaseInfo(baseInfo))

                    // 以下获取详细结果
                    result.detailText = buildMarkdown {
                        val symbols = baseInfo.getJSONArray("symbols").getJSONObject(0)
                        if (symbols.getString("ph_en")!=""){
                            addBold("注音：")
                            addText(symbols.getString("ph_en"))
                            commitLine()
                        }else if (symbols.getString("ph_am")!=""){
                            addBold("注音：")
                            addText(symbols.getString("ph_am"))
                            commitLine()
                        }

                        if(baseInfo.has("baesElse")){
                            addBold("大小写变形：")
                            commitLine()
                            val baseElses = baseInfo.getJSONArray("baesElse")
                            for (i in 0 until baseElses.length()){
                                val s = parseBaseInfo(baseElses.getJSONObject(i), isDetail = true)
                                addText(s)
                            }
                            commitLine()
                        }

                        // 同义词
                        if (message.has("synonym")){
                            addBold("同义词：")
                            commitLine()
                            val synonym = message.getJSONArray("synonym")
                            for (i in 0 until synonym.length()){
                                val eachSyn = synonym.getJSONObject(i)
                                val partName = eachSyn.getString("part_name")
                                if (partName!=""){
                                    addBold(partName)
                                    commitLine()
                                    val means = eachSyn.getJSONArray("means")
                                    for (j in 0 until means.length()){
                                        val eachMean = means.getJSONObject(j)
                                        addText(eachMean.getString("word_mean"))
                                        commitLine()
                                        val cis = eachMean.getJSONArray("cis")
                                        for (k in 0 until cis.length()){
                                            val word = cis.getString(k)
                                            addLink(word, "https://www.iciba.com/word?w=$word")
                                            addText(" ")
                                        }
                                        commitLine()
                                    }
                                }
                                commitLine()
                            }
                        }

                        if (message.has("new_sentence")){
                            addBold("例句：")
                            commitLine()
                            val sentences = message.getJSONArray("new_sentence").getJSONObject(0).getJSONArray("sentences")
                            for (i in 0 until min(sentences.length(), 5)){
                                val eachSentence = sentences.getJSONObject(i)
                                addBold("${i+1}.")
                                addText(eachSentence.getString("cn"))
                                commitLine()
                                addText(eachSentence.getString("en"))
                                addText(" (${eachSentence.getString("from")})")
                                commitLine()
                                commitLine()
                            }
                        }
//                        commitLine()
                        addLink("查看详情","https://www.iciba.com/word?w=${URLEncoder.encode(sourceString,"utf-8")}")
                    } // buildMarkdownText

                } else {
                    result.setBasicResult(baseInfo.getString("translate_result"))
                }
            } else if (all.getInt("status") == 10001) {
                result.setBasicResult("错误的访问！【" + all.getString("message") + "】")
            } else {
                throw TranslationException(Consts.ERROR_DATED_ENGINE)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            throw TranslationException(Consts.ERROR_JSON)
        }
    }

    //2020/12/26 可用,但太简单
    //http://dict-co.iciba.com/api/dictionary.php?w=go&key=0EAE08A016D6688F64AB3EBB2337BFB0
    //    9AA9FA4923AC16CED1583C26CF284C3F
    //2021.3.23 破解金山翻译
    //参见爬取金山翻译.py
    private val client = 6
    private val key = 1000006
    override fun madeURL(): String {
        val from = languageMapping[sourceLanguage]
        val to = languageMapping[targetLanguage]
        var url = ""
        try {
            val word = URLEncoder.encode(sourceString, "utf-8").replace("\\+".toRegex(), "%20")
            val time = System.currentTimeMillis()
            // url = String.format("http://www.iciba.com/index.php?a=getWordMean&c=search&word=%s",URLEncoder.encode(sourceString,"utf-8"));
            //url = String.format("http://fy.iciba.com/ajax.php?a=fy&f=%s&t=%s&w=%s",from,to, URLEncoder.encode(sourceString,"utf-8"));
            url = "https://dict.iciba.com/dictionary/word/query/web?" +
                    "client=" + client +
                    "&key=" + key +
                    "&timestamp=" + time +
                    "&word=" + URLEncoder.encode(word, "utf-8") +
                    "&signature=" + getSignature(word, time)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return url
    }

    override val isOffline: Boolean
        get() = false

    private fun getSignature(word: String, time: Long): String? {
        //t : '610000061616418129580nice%20to%20meet%20you.'
        val t =
            String.format(Locale.CHINA, "%d%d%s%s", client, key, time, word)
        //param :"/dictionary/word/query/web610000061616418129580nice%20to%20meet%20you.7ece94d9f9c202b0d2ec557dg4r9bc"
        val param =
            "/dictionary/word/query/web" + t + "7ece94d9f9c202b0d2ec557dg4r9bc"
        return getMD5LowerCase(param)
    }

    /**
     * 对字符串md5加密
     *
     * @param str 传入要加密的字符串
     * @return MD5加密后的字符串(小写+字母)
     */
    private fun getMD5LowerCase(str: String): String? {
        return try {
            // 生成一个MD5加密计算摘要
            val md = MessageDigest.getInstance("MD5")
            // 计算md5函数
            md.update(str.toByteArray())
            // digest()最后确定返回md5 hash值，返回值为8为字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
            // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
            BigInteger(1, md.digest()).toString(16)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}