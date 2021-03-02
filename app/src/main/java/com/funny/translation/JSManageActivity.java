package com.funny.translation;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.funny.translation.db.DBJS;
import com.funny.translation.db.DBJSUtils;
import com.funny.translation.js.JS;
import com.funny.translation.widget.JSManageAdapter;

public class JSManageActivity extends BaseActivity{
    RecyclerView rv;
    JSManageAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_js_manage);

        rv = findViewById(R.id.js_manage_rv);
        adapter = new JSManageAdapter(R.layout.view_js_manage_rv_item);
        adapter.setList(DBJSUtils.getInstance().queryAllJS());
        adapter.setEmptyView(R.layout.view_js_manage_rv_empty);
        adapter.addChildClickViewIds(R.id.view_js_manage_rv_item_check);
        adapter.setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                JSManageAdapter jsManageAdapter = (JSManageAdapter)adapter;
                switch (view.getId()){

                    case R.id.view_js_manage_rv_item_check:
                        DBJSUtils dbjsUtils = DBJSUtils.getInstance();
                        JS js = jsManageAdapter.getData().get(position);
                        js.enabled = 1 - js.enabled;
                        dbjsUtils.setJSEnabled(js.id,js.enabled);
                        break;
                }
            }
        });
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
    }
}
