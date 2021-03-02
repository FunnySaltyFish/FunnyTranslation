package com.funny.translation.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;

import com.funny.translation.R;
import com.funny.translation.utils.BitmapUtil;

import java.util.Arrays;
import java.util.List;

/**
 * 抽屉adapter
 * Created by zly on 2016/3/30.
 */
public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.DrawerViewHolder> {
    Context ctx;
    private static final int TYPE_DIVIDER = 0;
    private static final int TYPE_NORMAL = 1;
    private static final int TYPE_HEADER = 2;
    public DrawerAdapter(Context ctx){
        this.ctx = ctx;
    }

    private List<DrawerItem> dataList = Arrays.asList(
            new DrawerItemHeader(),
            new DrawerItemNormal(R.drawable.ic_setting,R.string.setting),
            new DrawerItemNormal(R.drawable.ic_feedback,R.string.feedback),
            new DrawerItemNormal(R.drawable.ic_apps,R.string.other_apps),
            new DrawerItemDivider(),
            new DrawerItemNormal(R.drawable.ic_js,R.string.js),
            new DrawerItemDivider()
            //            new DrawerItemNormal(R.mipmap.icon_drawerlayout_night, R.string.drawer_menu_night),
//            new DrawerItemNormal(R.mipmap.icon_drawerlayout_offline, R.string.drawer_menu_offline)
    );


    @Override
    public int getItemViewType(int position) {
        DrawerItem drawerItem = dataList.get(position);
        if (drawerItem instanceof DrawerItemDivider) {
            return TYPE_DIVIDER;
        } else if (drawerItem instanceof DrawerItemNormal) {
            return TYPE_NORMAL;
        }else if(drawerItem instanceof DrawerItemHeader){
            return TYPE_HEADER;
        }
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return (dataList == null || dataList.size() == 0) ? 0 : dataList.size();
    }

    @Override
    public DrawerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        DrawerViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_DIVIDER:
                viewHolder = new DividerViewHolder(inflater.inflate(R.layout.view_drawer_divider, parent, false));
                break;
            case TYPE_HEADER:
                viewHolder = new HeaderViewHolder(inflater.inflate(R.layout.view_drawer_header, parent, false));
                break;
            case TYPE_NORMAL:
                viewHolder = new NormalViewHolder(inflater.inflate(R.layout.view_drawer_normal, parent, false));
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(DrawerViewHolder holder, int position) {
        final DrawerItem item = dataList.get(position);
        if (holder instanceof NormalViewHolder) {
            NormalViewHolder normalViewHolder = (NormalViewHolder) holder;
            final DrawerItemNormal itemNormal = (DrawerItemNormal) item;
            normalViewHolder.iv.setBackgroundResource(itemNormal.iconRes);
            normalViewHolder.tv.setText(itemNormal.titleRes);

            normalViewHolder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        listener.itemClick(itemNormal);

                    }
                }
            });
        }else if(holder instanceof HeaderViewHolder){
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            ImageView img = holder.imageView;
            img.setImageBitmap(BitmapUtil.getCircleBitmap(BitmapUtil.getBitmapFromResources(ctx.getResources(),R.drawable.ic_launcher)));

            TextView headerTV = holder.textView;
            headerTV.setText(R.string.app_name);
        }

    }

    public OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

    public interface OnItemClickListener{
        void itemClick(DrawerItemNormal drawerItemNormal);
    }




    //-------------------------item数据模型------------------------------
    // drawerlayout item统一的数据模型
    public interface DrawerItem {
    }


    //有图片和文字的item
    public class DrawerItemNormal implements DrawerItem {
        public int iconRes;
        public int titleRes;

        public DrawerItemNormal(int iconRes, int titleRes) {
            this.iconRes = iconRes;
            this.titleRes = titleRes;
        }

    }

    //分割线item
    public class DrawerItemDivider implements DrawerItem {
        public DrawerItemDivider() {
        }
    }

    //头部item
    public class DrawerItemHeader implements DrawerItem{
        public DrawerItemHeader() {
        }
    }



    //----------------------------------ViewHolder数据模型---------------------------
    //抽屉ViewHolder模型
    public class DrawerViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        public DrawerViewHolder(View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.view_drawer_header_img);
            textView=itemView.findViewById(R.id.view_drawer_header_tv);
        }
    }

    //有图标有文字ViewHolder
    public class NormalViewHolder extends DrawerViewHolder {
        public View view;
        public TextView tv;
        public ImageView iv;

        public NormalViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            tv = itemView.findViewById(R.id.view_drawer_normal_tv);
            iv = itemView.findViewById(R.id.view_drawer_normal_img);
        }
    }

    //分割线ViewHolder
    public class DividerViewHolder extends DrawerViewHolder {

        public DividerViewHolder(View itemView) {
            super(itemView);
        }
    }

    //头部ViewHolder
    public class HeaderViewHolder extends DrawerViewHolder {

        private ImageView sdv_icon;
        private TextView tv_login;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            sdv_icon = itemView.findViewById(R.id.view_drawer_header_img);
            tv_login = itemView.findViewById(R.id.view_drawer_header_tv);
        }
    }
}

