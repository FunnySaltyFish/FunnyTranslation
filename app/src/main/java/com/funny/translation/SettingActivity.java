package com.funny.translation;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.funny.translation.bean.Consts;
import com.funny.translation.utils.ApplicationUtil;
import com.funny.translation.utils.DataUtil;
import com.funny.translation.utils.SharedPreferenceUtil;
import com.funny.translation.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static com.funny.translation.bean.Consts.LANGUAGES;

public class SettingActivity extends BaseActivity
{
    Context ctx;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ctx = this;
        if(savedInstanceState==null){
            FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
            ft.add(R.id.activity_setting_layout,new SettingFragment());
            ft.commit();
        }
    }

    public static class SettingFragment extends PreferenceFragmentCompat
    {
        Preference pre_language_sort;
        Preference.OnPreferenceClickListener listener;
        Resources res;
        ArrayList mSortedList;
        Context ctx;
        @Override
        public void onCreatePreferences(Bundle p1, String p2){
            addPreferencesFromResource(R.xml.preference_setting);
            res=getContext().getResources();
            ctx=getContext();
            pre_language_sort=getPreferenceManager().findPreference("preference_language_sort");
            listener=new Preference.OnPreferenceClickListener(){
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
            if(pre_language_sort!=null){pre_language_sort.setOnPreferenceClickListener(listener);}
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        private void showSortDialog(){
            RecyclerView recyclerView=new RecyclerView(Objects.requireNonNull(getContext()));
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
                public int getMovementFlags(RecyclerView recyclerView, android.support.v7.widget.RecyclerView.ViewHolder viewHolder) {
                    //首先回调的方法,返回int表示是否监听该方向
                    int dragFlag = ItemTouchHelper.DOWN | ItemTouchHelper.UP;//拖拽
                    int swipeFlag = 0;//侧滑删除
                    return makeMovementFlags(dragFlag, swipeFlag);
                }

                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, android.support.v7.widget.RecyclerView.ViewHolder target) {
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

                public void clearView(RecyclerView recyclerView, android.support.v7.widget.RecyclerView.ViewHolder viewHolder) {
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
                            ApplicationUtil.print(ctx,"已保存！将在下次重启App时生效！");
                        }
                    })
                    .create();
            dialog.show();
        }
    }
}
