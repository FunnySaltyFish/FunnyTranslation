package com.funny.translation.helper

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.TextView
import android.widget.Toast
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

fun Context.showMessageDialog(
    title: String,
    message: String,
    isMarkdown: Boolean = false,
    positiveAction: () -> Unit = {},
    positiveText: String? = "确定",
    negativeAction: () -> Unit = {},
    negativeText: String? = "取消"
) {
    val ctx = this
    val builder = AlertDialog.Builder(this).apply {
        val builder = this
        setTitle(title)
        if (!isMarkdown) setMessage(message)
        else {
            val tv = TextView(ctx).apply {
                setPadding(16, 24, 16, 8)
            }
            val markdown = MarkdownUtils.getDefaultMarkwon(ctx)
            markdown.setMarkdown(tv, message)
            builder.setView(tv)
        }
        if (!positiveText.isNullOrEmpty()) {
            builder.setPositiveButton(positiveText) { _, _ -> positiveAction() }
        }
        if (!negativeText.isNullOrEmpty()) {
            builder.setNegativeButton(negativeText) { _, _ -> negativeAction() }
        }
    }
    builder.show()
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
            startActivity(Intent.createChooser(intent, "请选择浏览器"))
        } catch (e: Exception) {
            toastOnUi(e.localizedMessage ?: "open url error")
        }
    }
}


fun Context.toastOnUi(message: Int) {
    runOnUI {
        ToastUtils.showShort(message)
    }
}

fun Context.toastOnUi(message: CharSequence?, length: Int = Toast.LENGTH_SHORT) {
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