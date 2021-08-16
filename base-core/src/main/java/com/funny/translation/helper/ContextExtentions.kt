package com.funny.translation.helper

import android.app.Activity
import android.content.Context
import android.content.Intent

fun Activity.startChooseFile(
    type : String = "application/javascript",
    requestCode : Int,
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