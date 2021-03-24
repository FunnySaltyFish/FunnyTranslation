package com.funny.translation;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.funny.translation.bean.Consts;
import com.funny.translation.db.DBJSUtils;
import com.funny.translation.js.JS;
import com.funny.translation.js.JSEngine;
import com.funny.translation.js.JSException;
import com.funny.translation.utils.ApplicationUtil;
import com.funny.translation.utils.FileUtil;
import com.funny.translation.widget.JSManageAdapter;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.io.IOException;

public class JSManageActivity extends BaseActivity {
    RecyclerView rv;
    JSManageAdapter adapter;

    FloatingActionButton btnImportFromLocal,btnNewFile;

    AlertDialog jsDetailDialog, deleteJSDialog;

    String TAG = "JSManageActivity";

    boolean hasChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_js_manage);

        rv = findViewById(R.id.js_manage_rv);
        adapter = new JSManageAdapter(R.layout.view_js_manage_rv_item);
        adapter.setList(DBJSUtils.getInstance().queryAllJS());

        adapter.addChildClickViewIds(R.id.view_js_manage_rv_item_check);
        adapter.setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                JSManageAdapter jsManageAdapter = (JSManageAdapter) adapter;

                switch (view.getId()) {

                    case R.id.view_js_manage_rv_item_check:
                        DBJSUtils dbjsUtils = DBJSUtils.getInstance();
                        JS js = jsManageAdapter.getData().get(position);
                        js.enabled = 1 - js.enabled;
                        dbjsUtils.setJSEnabled(js.id, js.enabled);
                        hasChanged = true;
                        break;

                }
            }
        });
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                JS js = ((JSManageAdapter) adapter).getItem(position);
                showJSDetailDialog(js);
            }
        });
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter.setUseEmpty(true);
        adapter.setEmptyView(R.layout.view_js_manage_rv_empty);
        initFAB();
    }

    private void initFAB(){
        btnImportFromLocal = findViewById(R.id.js_manage_import_from_local);
        btnImportFromLocal.setOnClickListener((View view)->{
            startChooseFile();
        });

        btnNewFile = findViewById(R.id.js_manage_new_file);
        btnNewFile.setOnClickListener((View view)->{
            moveToActivity(CodeActivity.class);
        });
    }

    private void startChooseFile(){
        //通过系统的文件浏览器选择一个文件
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        //筛选，只显示可以“打开”的结果，如文件(而不是联系人或时区列表)
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        //过滤只显示图像类型文件
        //intent.setType("text/javascript");
        intent.setType("application/javascript");
        startActivityForResult(intent, Consts.ACTIVITY_JS_MANAGE);

    }

    private void showJSDetailDialog(JS js) {
        jsDetailDialog = new AlertDialog.Builder(JSManageActivity.this)
                .setTitle(js.fileName)
                .setMessage(String.format("关于：\n%s", js.about))
                .setNegativeButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showDeleteJSDialog(js);
                    }
                })
                .create();

        jsDetailDialog.show();
    }

    private void showDeleteJSDialog(JS js) {
        deleteJSDialog = new AlertDialog.Builder(JSManageActivity.this)
                .setTitle("警告")
                .setMessage(String.format("您确定要删除插件【%s】吗？", js.fileName))
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DBJSUtils.getInstance().deleteJS(js);
                        Log.i(TAG, "当前准备删除的js ID为：" + js.id);
                        adapter.remove(js);
                        ApplicationUtil.print(getBaseContext(), "已删除！");
                        hasChanged = true;
                    }
                })
                .setCancelable(false)
                .create();
        deleteJSDialog.show();
    }

    @Override
    public void onBackPressed() {
        Intent backIntent = new Intent();
        backIntent.putExtra("hasChanged", hasChanged);
        setResult(Consts.ACTIVITY_JS_MANAGE, backIntent);
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if(requestCode == Consts.ACTIVITY_JS_MANAGE && resultCode == Activity.RESULT_OK){
            Uri uri = null;
            if (resultData != null) {
                // 获取选择文件Uri
                uri = resultData.getData();
                if (uri != null){
                    try{
                        String code = FileUtil.readTextFromUri(this,uri);
                        //Log.i(TAG,"加载进来的code是：\n"+code);
                        JS js = new JS(code);
                        js.id = DBJSUtils.getInstance().getNextID();
                        JSEngine jsEngine = new JSEngine();
                        jsEngine.loadJS(js);
                        jsEngine.loadBasicConfigurations();

                        adapter.addData(jsEngine.js);
                        DBJSUtils.getInstance().insertJS(jsEngine.js);
                        hasChanged = true;
                        ApplicationUtil.print(this,"添加成功！");
                    }catch (IOException e){
                        e.printStackTrace();
                        ApplicationUtil.print(this,"添加插件时发生IO流错误，添加失败。");
                    }catch (JSException e){
                        e.printStackTrace();
                        ApplicationUtil.print(this,"添加插件时插件本身产生错误，原因是："+e.getMessage());
                    }
                }
            }

        }
    }
}
