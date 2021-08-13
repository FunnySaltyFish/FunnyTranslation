package com.funny.translation.widget

import com.chad.library.adapter.base.BaseQuickAdapter
import com.funny.translation.js.bean.JsBean
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.funny.translation.R
import android.widget.CheckBox

class JSManageAdapter(layoutResId: Int) : BaseQuickAdapter<JsBean, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: JsBean) {
        holder.setText(R.id.view_js_manage_rv_item_author, item.author)
        holder.setText(R.id.view_js_manage_rv_item_name, item.fileName)
        holder.setText(R.id.view_js_manage_rv_item_version, "版本：" + item.version)
        val checkBox: CheckBox = holder.getView(R.id.view_js_manage_rv_item_check)
        checkBox.isChecked = item.enabled == 1
    }
}