package com.funny.translation.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.funny.translation.R;
import com.funny.translation.db.DBEnglishWords;
import com.funny.translation.db.DBEnglishWordsUtils;

import java.util.ArrayList;

public class WordCompleteAdapter extends RecyclerView.Adapter {
    ArrayList<DBEnglishWords.Word> words;
    Context ctx;

    public WordCompleteAdapter(Context ctx, ArrayList<DBEnglishWords.Word> list){
        this.ctx=ctx;
        this.words=list;
    }

    public void setListener(LanguageAdapter.OnClickItemListener listener) {
        this.listener = listener;
    }

    public void update(ArrayList<DBEnglishWords.Word> list){
        this.words=list;
        notifyDataSetChanged();
    }

    LanguageAdapter.OnClickItemListener listener;
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.view_popup_list_item,viewGroup,false);
        return new SingleItemVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int i) {
        if(viewHolder instanceof SingleItemVH){
            SingleItemVH vh = (SingleItemVH)viewHolder;
            DBEnglishWords.Word word = words.get(i);
            vh.word.setText(word.getWord());
            vh.detail.setText(word.getPhoneticSymbol()+" "+word.getExplanation());
            vh.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener!=null){
                        listener.onClick(viewHolder.getAdapterPosition());
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return words==null?0:words.size();
    }

    public void setWords(ArrayList<DBEnglishWords.Word> words) {
        this.words = words;
    }

    public ArrayList<DBEnglishWords.Word> getWords() {
        return words;
    }

    class SingleItemVH extends RecyclerView.ViewHolder{
        TextView word;
        TextView detail;
        public SingleItemVH(@NonNull View itemView) {
            super(itemView);
            word = itemView.findViewById(R.id.view_popup_word);
            detail = itemView.findViewById(R.id.view_popup_word_detail);
        }
    }
}
