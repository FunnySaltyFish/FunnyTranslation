package com.funny.translation.translate.database

import android.util.Log
import com.funny.translation.helper.readAssets
import com.funny.translation.js.JsEngine
import com.funny.translation.js.bean.JsBean
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.engine.ImageTranslationEngines
import com.funny.translation.translate.engine.TextTranslationEngines
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

object DefaultData {
    private val TAG = "DefaultData"

    fun isPluginBound(jsBean: JsBean) = bindEngines.any { it.name == jsBean.fileName }

    val bindEngines = listOf(
        TextTranslationEngines.BaiduNormal,
        TextTranslationEngines.Tencent,
        TextTranslationEngines.Youdao,
        TextTranslationEngines.Jinshan,

        TextTranslationEngines.BiggerText,
        TextTranslationEngines.EachText,
        TextTranslationEngines.Bv2Av
    )

    val bindImageEngines = listOf(ImageTranslationEngines.Baidu, ImageTranslationEngines.Tencent)
}