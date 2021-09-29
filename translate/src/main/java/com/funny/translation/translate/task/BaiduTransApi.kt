package com.funny.translation.translate.task

import com.funny.translation.trans.TranslationException
import com.funny.translation.translate.extentions.md5
import java.util.HashMap

class BaiduTransApi(private val appid: String, private val securityKey: String) {
    @Throws(TranslationException::class)
    fun getTransResult(query: String, from: String, to: String): String {
        val params = buildParams(query, from, to)
        return BaiduHttpGet.get(TRANS_API_HOST, params)
    }

    private fun buildParams(query: String, from: String, to: String): Map<String, String> {
        val params: MutableMap<String, String> = HashMap()
        params["q"] = query
        params["from"] = from
        params["to"] = to
        params["appid"] = appid

        // 随机数
        val salt = System.currentTimeMillis().toString()
        params["salt"] = salt

        // 签名
        val src = appid + query + salt + securityKey // 加密前的原文
        params["sign"] = MD5.md5(src)
        return params
    }

    companion object {
        private const val TRANS_API_HOST = "http://api.fanyi.baidu.com/api/trans/vip/translate"
        private var api: BaiduTransApi? = null
        fun getBaiduTransApi(appid: String, securityKey: String): BaiduTransApi {
            if (api == null) {
                api = BaiduTransApi(appid, securityKey)
            }
            return api!!
        }
    }
}