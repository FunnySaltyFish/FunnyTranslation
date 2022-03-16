package com.funny.translation.translate.database

import android.util.Log
import com.funny.translation.helper.readAssets
import com.funny.translation.js.JsEngine
import com.funny.translation.js.bean.JsBean
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.engine.TranslationEngines
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

object DefaultData {
    private val TAG = "DefaultData"
    suspend fun getDefaultJsList(scope: CoroutineScope): List<JsBean> {
        var arr by Delegates.notNull<List<JsBean>>()
        Log.d(TAG, "getDefaultJsList: 开始执行")
        scope.launch {
            withContext(Dispatchers.IO) {
                val fileNames = arrayOf("谷歌翻译实现.js")
                arr = fileNames.map {
                    val code = FunnyApplication.ctx.readAssets(it)
                    val jsBean = JsBean(code = code)
                    val jsEngine = JsEngine(jsBean)
                    jsEngine.loadBasicConfigurations({
                        Log.d(TAG, "getDefaultJsList: 加载js引擎完毕")
                    }, { e ->
                        Log.d(TAG, "loadDefaultData Failed!: ${e.message}")
                    })
                    jsEngine.jsBean
                }
            }
        }.join()
        Log.d(TAG, "getDefaultJsList: 准备返回js引擎列表")
        return arr
    }

    val bindEngines = listOf(
        TranslationEngines.BaiduNormal,
        TranslationEngines.Youdao,
        TranslationEngines.Jinshan,

        TranslationEngines.BiggerText,
        TranslationEngines.EachText,
        TranslationEngines.Bv2Av
    )
}