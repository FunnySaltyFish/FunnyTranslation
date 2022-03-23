package com.funny.translation;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.funny.translation.bean.App;
import com.funny.translation.widget.OtherAppsAdapter;

import java.util.ArrayList;

public class OtherApplicationsActivity extends BaseActivity{
    RecyclerView rv;
    OtherAppsAdapter adapter;
    Resources res;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_applications);
        res=getResources();
        rv=findViewById(R.id.activity_other_applications_rv);

        ArrayList<App> apps=new ArrayList<>();
        apps.add(new App(R.drawable.app_funnyluckyman,
                res.getString(R.string.app_funnyluckyman_name),
                res.getString(R.string.app_funnyluckyman_introduction),
                res.getString(R.string.app_funnyluckyman_url)
        ));

        adapter=new OtherAppsAdapter(R.layout.view_other_applications_item,apps);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                OtherAppsAdapter adapter1 = (OtherAppsAdapter)adapter;
                openURL(adapter1.getData().get(position).getUrl());
            }
        });

        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this));
    }

    private void openURL(String url){
        Intent intent=new Intent();
        Uri uri= Uri.parse(url);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(uri);
        this.startActivity(intent);
    }

}
