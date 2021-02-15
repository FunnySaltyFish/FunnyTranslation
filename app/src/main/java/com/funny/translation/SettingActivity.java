package com.funny.translation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.funny.translation.bean.Consts;
import com.funny.translation.utils.ApplicationUtil;
import com.funny.translation.utils.DataUtil;
import com.funny.translation.utils.SharedPreferenceUtil;
import com.funny.translation.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Objects;

import static com.funny.translation.bean.Consts.LANGUAGES;

public class SettingActivity extends BaseActivity
{
    Context ctx;
    public boolean isRvChange=false;
    public boolean isDiyBaiduChange=false;
    Intent backIntent=new Intent();

    String TAG = "SettingActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        setContentView(R.layout.activity_setting);
        ctx = this;
        if(savedInstanceState==null){
            FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
            ft.add(R.id.activity_setting_layout,new SettingFragment());
            ft.commit();
        }
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG,"onBackPressed");
        backIntent.putExtra("isRvChange",isRvChange);
        backIntent.putExtra("isDiyBaiduChange",isDiyBaiduChange);
        setResult(Consts.ACTIVITY_SETTING,backIntent);
        super.onBackPressed();
    }

    public static class SettingFragment extends PreferenceFragmentCompat
    {
        Preference pre_language_sort,pre_source_language,pre_target_language,pre_is_diy_baidu,pre_engines;
        Preference.OnPreferenceClickListener preferenceClickListener;
        Preference.OnPreferenceChangeListener preferenceChangeListener;
        Resources res;
        ArrayList mSortedList;
        SettingActivity ctx;
        private String TAG="SettingFragment";

        @Override
        public void onCreatePreferences(Bundle p1, String p2){
            addPreferencesFromResource(R.xml.preference_setting);
            res=getContext().getResources();
            ctx= (SettingActivity) getActivity();
            pre_language_sort=getPreferenceManager().findPreference("preference_language_sort");
            pre_source_language=getPreferenceManager().findPreference("preference_language_source_default");
            pre_target_language=getPreferenceManager().findPreference("preference_language_target_default");
            pre_engines=getPreferenceManager().findPreference("preference_engines_default");
            pre_is_diy_baidu = getPreferenceManager().findPreference("preference_baidu_is_diy_api");
            preferenceClickListener =new Preference.OnPreferenceClickListener(){
                @Override
                public boolean onPreferenceClick(Preference p){
                    switch(p.getKey()){
                        case "preference_language_sort":
                            showSortDialog();
                            return true;
                    }
                    return false;
                }
            };
            preferenceChangeListener=new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    switch (preference.getKey()){
                        case "preference_language_source_default":
                        case "preference_language_target_default":
                        case "preference_engines_default":
                            ctx.isRvChange=true;
                            return true;
                        case "preference_baidu_is_diy_api":
                            //Log.i(TAG,"____prechange，已设置。");
                            ctx.isDiyBaiduChange=true;
                            return true;
                    }
                    return true;
                }
            };
            if(pre_language_sort!=null){pre_language_sort.setOnPreferenceClickListener(preferenceClickListener);}
            pre_target_language.setOnPreferenceChangeListener(preferenceChangeListener);
            pre_source_language.setOnPreferenceChangeListener(preferenceChangeListener);
            pre_engines.setOnPreferenceChangeListener(preferenceChangeListener);
            pre_is_diy_baidu.setOnPreferenceChangeListener(preferenceChangeListener);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

        }

        private void showSortDialog(){
            RecyclerView recyclerView=new RecyclerView(requireContext());
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            params.setMargins(12,0,12,0);
            recyclerView.setLayoutParams(params);

            String pre_language_mapping_string = SharedPreferenceUtil.getInstance().getString("pre_language_mapping_string","");
            int[] languageMapping;
            if (pre_language_mapping_string.equals("")){
                languageMapping=new int[LANGUAGES.length];
                DataUtil.setDefaultMapping(languageMapping);
            }
            else{
                languageMapping = DataUtil.coverStringToIntArray(pre_language_mapping_string);
            }
            String[] defaultArr = res.getStringArray(R.array.languages);
            String[] arr = new String[defaultArr.length];
            for (int i = 0; i < defaultArr.length; i++) {
                arr[i] = defaultArr[languageMapping[i]];
            }

            final SimpleAdapter mAdapter = new SimpleAdapter(getContext(),
                    arr);
            recyclerView.setAdapter(mAdapter);
            ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
                @Override
                public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                    //首先回调的方法,返回int表示是否监听该方向
                    int dragFlag = ItemTouchHelper.DOWN | ItemTouchHelper.UP;//拖拽
                    int swipeFlag = 0;//侧滑删除
                    return makeMovementFlags(dragFlag, swipeFlag);
                }

                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    if (mAdapter != null) {
                        mAdapter.onMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                    }
                    return true;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

                }

                public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                    if (actionState != 0) {
                        viewHolder.itemView.setAlpha(0.9f);
                    }
                    super.onSelectedChanged(viewHolder, actionState);
                }

                public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                    super.clearView(recyclerView, viewHolder);
                    viewHolder.itemView.setAlpha(1.0f);
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                        mSortedList = mAdapter.getSortedDataList();
                    }
                }
            };
            ItemTouchHelper itemTouchHelper=new ItemTouchHelper(callback);
            itemTouchHelper.attachToRecyclerView(recyclerView);

            AlertDialog dialog=new AlertDialog.Builder(getContext())
                    .setView(recyclerView)
                    .setTitle("长按拖动更改排序")
                    .setNegativeButton("取消",null )
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            int[] mapping = new int[mSortedList.size()];
                            for (int j = 0; j < mapping.length; j++) {
                                mapping[j] = DataUtil.findStringIndex(Consts.LANGUAGE_NAMES,mSortedList.get(j).toString());
                            }
                            SharedPreferenceUtil.getInstance().putString("pre_language_mapping_string",DataUtil.coverIntArrayToString(mapping));
                            ApplicationUtil.print(ctx,"已保存！即刻生效！");
                            ctx.isRvChange=true;
                        }
                    })
                    .create();
            dialog.show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                backIntent.putExtra("isRvChange",isRvChange);
                backIntent.putExtra("isDiyBaiduChange",isDiyBaiduChange);
                setResult(Consts.ACTIVITY_SETTING,backIntent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
