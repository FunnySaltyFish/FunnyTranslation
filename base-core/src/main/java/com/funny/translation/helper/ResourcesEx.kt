package com.funny.translation.helper

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

private const val PREFIX_ASSETS_KEY = "__assets__"
private const val TAG = "ResourcesEx"

@Composable
@ReadOnlyComposable
fun assetsString(name: String): String {
    val context = LocalContext.current
    val assetsKey = PREFIX_ASSETS_KEY + name
    val readData = DataHolder.get<String?>(assetsKey)
    return if (readData != null) {
        readData
    } else {
        val readAssets = context.readAssets(name)
        DataHolder.put(assetsKey, readAssets)
        readAssets
    }
}

/**
 * 带 i18n 的 assetsString，文件名应当形如 xxx_zh.json
 * 如果找不到带语言的文件，则使用默认的文件
 * @param name String
 * @return String
 */
@Composable
@ReadOnlyComposable
fun assetsStringLocalized(name: String): String {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locale
    val localedAssetsName = name.addBeforeFileEx("_" + locale.language)
    Log.d(TAG, "assetsStringLocalized: try to find $localedAssetsName")
    return if (context.assets.list("")?.contains(localedAssetsName) == true) {
        assetsString(localedAssetsName)
    } else {
        assetsString(name)
    }
}

private fun String.addBeforeFileEx(text: String): String {
    val index = lastIndexOf(".")
    return if (index > 0) {
        substring(0, index) + text + substring(index)
    } else {
        this
    }
}