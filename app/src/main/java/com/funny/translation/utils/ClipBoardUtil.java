package com.funny.translation.utils;

import android.content.Context;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.text.TextUtils;

/**
 * 剪切板读写工具
 */
public class ClipBoardUtil {
    /**
     * 获取剪切板内容
     * @return
     */
    public static String get(Context ctx){
        ClipboardManager manager = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager != null) {
            if (manager.hasPrimaryClip() && manager.getPrimaryClip().getItemCount() > 0) {
                CharSequence addedText = manager.getPrimaryClip().getItemAt(0).getText();
                String addedTextString = String.valueOf(addedText);
                if (!TextUtils.isEmpty(addedTextString)) {
                    return addedTextString;
                }
            }
        }
        return "";
    }

    public static void copy(Context ctx,String content){
        //获取剪贴板管理器：
        // 获取系统剪贴板
        ClipboardManager clipboard = (ClipboardManager)ctx.getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建一个剪贴数据集，包含一个普通文本数据条目（需要复制的数据）
        ClipData clipData = ClipData.newPlainText(null,content);
        // 把数据集设置（复制）到剪贴板
        clipboard.setPrimaryClip(clipData);

		/*
		 来自：https://www.jianshu.com/p/1e84d33154bd
		 */
    }

    /**
     * 清空剪切板
     */
    public static void clear(Context ctx){
        ClipboardManager manager = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager != null) {
            try {
                manager.setPrimaryClip(manager.getPrimaryClip());
                manager.setPrimaryClip(ClipData.newPlainText("",""));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

