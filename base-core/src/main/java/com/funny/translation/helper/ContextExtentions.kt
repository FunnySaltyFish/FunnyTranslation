package com.funny.translation.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.funny.translation.core.R
import com.funny.translation.helper.handler.runOnUI
import com.hjq.toast.ToastUtils
import java.io.File
import java.io.InputStream

private const val TAG = "ContextExtensions"
fun Activity.startChooseFile(
    type: String = "application/javascript",
    requestCode: Int,
) {
    //通过系统的文件浏览器选择一个文件
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
    //筛选，只显示可以“打开”的结果，如文件(而不是联系人或时区列表)
    intent.addCategory(Intent.CATEGORY_OPENABLE)
    //过滤只显示图像类型文件
    //intent.setType("text/javascript");
    intent.type = type
    this.startActivityForResult(intent, requestCode)
}

fun Context.readAssets(fileName: String): String {
    var ins: InputStream? = null
    return try {
        ins = assets.open(fileName)
        String(ins.readBytes())
    } catch (e: Exception) {
        ""
    } finally {
        try {
            ins?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun Context.openUrl(url: String) {
    openUrl(Uri.parse(url))
}

fun Context.openUrl(uri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = uri

    try {
        startActivity(intent)
    } catch (e: Exception) {
        try {
            startActivity(Intent.createChooser(intent, getString(R.string.please_choose_browser)))
        } catch (e: Exception) {
            toastOnUi(e.localizedMessage ?: "open url error")
        }
    }
}


inline fun Context.toastOnUi(message: Int, length: Int = Toast.LENGTH_SHORT) {
    toastOnUi(getString(message), length)
}

// 内联以使得框架能够获取到调用的真正位置
inline fun Context.toastOnUi(message: CharSequence?, length: Int = Toast.LENGTH_SHORT) {
    runOnUI {
        if (length == Toast.LENGTH_SHORT) {
            ToastUtils.showShort(message)
        } else {
            ToastUtils.showLong(message)
        }
    }
}

val Context.externalCache: File
    get() = this.externalCacheDir ?: this.cacheDir