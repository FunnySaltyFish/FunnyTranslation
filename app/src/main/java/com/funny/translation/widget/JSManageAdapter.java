package com.funny.translation.widget;

import android.widget.CheckBox;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.funny.translation.R;
import com.funny.translation.js.JS;

import org.jetbrains.annotations.NotNull;

public class JSManageAdapter extends BaseQuickAdapter<JS,BaseViewHolder > {
    public JSManageAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, JS js) {
        baseViewHolder.setText(R.id.view_js_manage_rv_item_author,js.author);
        baseViewHolder.setText(R.id.view_js_manage_rv_item_name,js.fileName);
        baseViewHolder.setText(R.id.view_js_manage_rv_item_version,"版本："+js.version);
        CheckBox checkBox = baseViewHolder.findView(R.id.view_js_manage_rv_item_check);
        assert checkBox!=null;
        checkBox.setChecked(js.enabled==1);
    }
}
