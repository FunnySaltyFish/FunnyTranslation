package com.funny.translation.translate.database

import android.util.Log
import com.funny.translation.helper.readAssets
import com.funny.translation.js.JsEngine
import com.funny.translation.js.bean.JsBean
import com.funny.translation.translate.FunnyApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

object DefaultData {
    private val TAG = "DefaultData"
    suspend fun getDefaultJsList(scope: CoroutineScope): List<JsBean> {
        var arr by Delegates.notNull<List<JsBean>>()
        withContext(Dispatchers.IO) {
            val fileNames = arrayOf("谷歌翻译实现.js")
            arr = fileNames.map {
                val code = FunnyApplication.ctx.readAssets(it)
                val jsBean = JsBean(code = code)
                val jsEngine = JsEngine(jsBean)
                jsEngine.loadBasicConfigurations({

                }, { e ->
                    Log.d(TAG, "loadDefaultData Failed!: ${e.message}")
                })
                jsEngine.jsBean
            }
        }
        return arr
    }
}