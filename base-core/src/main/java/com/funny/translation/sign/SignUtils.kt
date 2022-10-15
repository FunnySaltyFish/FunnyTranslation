package com.funny.translation.sign

import com.funny.translation.BaseApplication
import com.funny.translation.helper.readAssets
import com.funny.translation.js.config.JsConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.script.Invocable


object SignUtils {
    private var hasInitJs = false
    fun encodeSign(uid : Long, appVersionCode: Int, sourceLanguageCode: Int, targetLanguageCode: Int, text: String, extra: String = "") = try {
        while (!hasInitJs){
            Thread.sleep(100)
        }
        (JsConfig.SCRIPT_ENGINE as? Invocable)?.invokeFunction(
            "encode_sign", maxOf(uid, 0L), appVersionCode, sourceLanguageCode, targetLanguageCode, text, extra
        ).toString()
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }

    /**
     * 加载签名脚本，鉴于之前发生过疑似接口盗用的情况，故而此处区分了正式发布版本和开源版本
     */
    suspend fun loadJs(){
        val assets = BaseApplication.ctx.assets
        val fileList = assets.list("")

        withContext(Dispatchers.IO){
            val jsText =
            if (fileList?.contains("funny_sign_v1_release.js") == true){
                BaseApplication.ctx.readAssets("funny_sign_v1_release.js")
            }else if (fileList?.contains("funny_sign_v1_open_source.js") == true){
                BaseApplication.ctx.readAssets("funny_sign_v1_open_source.js")
            }else ""
            if (jsText != ""){
                JsConfig.SCRIPT_ENGINE.eval(jsText)
            }
        }.also {
            hasInitJs = true
        }
    }
}