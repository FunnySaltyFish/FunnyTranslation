package com.funny.translation.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.funny.translation.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SimpleAdapter extends RecyclerView.Adapter {
    Context ctx;
    ArrayList<String> list;
    public SimpleAdapter(Context ctx, String[] list){
        this.ctx=ctx;
        this.list=new ArrayList<String>(Arrays.asList(list));
    }

    @NonNull
    @Override
    public SimpleHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.view_simple_adapter_item,viewGroup,false);
        SimpleHolder sh =new SimpleHolder(view);
        return sh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof  SimpleHolder){
            SimpleHolder sh = (SimpleHolder)viewHolder;
            sh.tv.setText(list.get(i));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void onMove(int fromPosition, int toPosition) {
        //对原数据进行移动
        Collections.swap(list, fromPosition, toPosition);
        //通知数据移动
        notifyItemMoved(fromPosition, toPosition);
    }

    public ArrayList getSortedDataList() {
        return this.list;
    }

    private class SimpleHolder extends RecyclerView.ViewHolder{
        TextView tv;
        public SimpleHolder(@NonNull View itemView) {
            super(itemView);
            tv=itemView.findViewById(R.id.view_simple_adapter_item_tv);
        }
    }
}
