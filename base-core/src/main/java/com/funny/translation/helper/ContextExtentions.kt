package com.funny.translation.helper

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.widget.TextView

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

fun Context.showMessageDialog(
    title: String,
    message : String,
    isMarkdown : Boolean = false,
    positiveAction : ()->Unit = {},
    positiveText : String? = "确定",
    negativeAction : ()->Unit,
    negativeText : String? = "取消"
){
    val ctx = this
    val builder = AlertDialog.Builder(this).apply {
        setTitle(title)
        if(!isMarkdown)setMessage(message)
        else{
            val tv = TextView(ctx).apply {
                setPadding(16,24,16,8)
            }
            val markdown = MarkdownUtils.getDefaultMarkwon(ctx)
            markdown.setMarkdown(tv,message)
            setView(tv)
            if(!positiveText.isNullOrBlank()) {
                setPositiveButton(positiveText) { _, _ -> positiveAction() }
            }
            if(!negativeText.isNullOrBlank()){
                setNegativeButton(negativeText){ _ , _ -> negativeAction() }
            }
        }
    }
    builder.show()
}