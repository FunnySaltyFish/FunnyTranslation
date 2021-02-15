package com.funny.translation.widget;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.funny.translation.bean.App;
import com.funny.translation.R;

import java.util.List;

public class OtherAppsAdapter extends BaseQuickAdapter<App,BaseViewHolder> {
    public OtherAppsAdapter(int layoutResId, @Nullable List<App> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, App app) {
        helper.setText(R.id.view_other_apps_item_name,app.getName())
                .setText(R.id.view_other_apps_item_introduction,app.getIntroduction())
                .setImageResource(R.id.view_other_apps_item_image,app.getImage());
    }
}
