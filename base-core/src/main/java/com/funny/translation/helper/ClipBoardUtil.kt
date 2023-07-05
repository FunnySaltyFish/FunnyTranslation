package com.funny.translation.helper

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.TextUtils
import java.lang.Exception


/**
 * 剪切板读写工具
 */
object ClipBoardUtil {
    /**
     * 获取剪切板内容
     * @return
     */
    fun read(ctx: Context): String {
        val manager = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return ""
        if (manager.hasPrimaryClip() && manager.primaryClip!!.itemCount > 0) {
            val addedText = manager.primaryClip!!.getItemAt(0).text
            val addedTextString = addedText.toString()
            if (!TextUtils.isEmpty(addedTextString)) {
                return addedTextString
            }
        }
        return ""
    }

    fun copy(ctx: Context, content: CharSequence?) {
        //获取剪贴板管理器：
        // 获取系统剪贴板
        val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        // 创建一个剪贴数据集，包含一个普通文本数据条目（需要复制的数据）
        val clipData = ClipData.newPlainText(null, content)
        // 把数据集设置（复制）到剪贴板
        clipboard.setPrimaryClip(clipData)

        /*
		 来自：https://www.jianshu.com/p/1e84d33154bd
		 */
    }

    /**
     * 清空剪切板
     */
    fun clear(ctx: Context) {
        val manager = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        try {
            manager.setPrimaryClip(manager.primaryClip!!)
            manager.setPrimaryClip(ClipData.newPlainText("", ""))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

