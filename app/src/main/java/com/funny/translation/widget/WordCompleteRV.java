package com.funny.translation.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.funny.translation.db.DBEnglishWords;

import java.util.ArrayList;

public class WordCompleteRV extends RecyclerView {
    public ArrayList<DBEnglishWords.Word> list;
    private WordCompleteAdapter adapter;
    public WordCompleteRV(@NonNull Context context) {
        this(context,null,0);
    }

    public WordCompleteRV(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context,attrs,0);
    }

    public WordCompleteRV(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
//        this.adapter = new WordCompleteAdapter(this);
//        setAdapter(adapter);
//        setLayoutManager(new LinearLayoutManager(context));
    }

    public void setAdapter(WordCompleteAdapter adapter) {
        this.adapter = adapter;
        super.setAdapter(adapter);
    }

    public ArrayList<DBEnglishWords.Word> getList() {
        return list;
    }

    public void setList(ArrayList<DBEnglishWords.Word> list) {
        this.list = list;
    }

    public void update(ArrayList<DBEnglishWords.Word> list){
        setList(list);
        this.adapter.notifyDataSetChanged();
    }
}
