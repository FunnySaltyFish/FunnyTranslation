package com.funny.translation.widget;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.funny.translation.R;
import com.funny.translation.bean.Consts;
import com.funny.translation.js.TranslationCustom;
import com.funny.translation.translation.BasicTranslationTask;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NewResultAdapter extends BaseQuickAdapter<BasicTranslationTask, NewResultAdapter.ResultContentHolder> {
    private final StringBuilder sb = new StringBuilder();
    public NewResultAdapter(int layoutResId, @Nullable List<BasicTranslationTask> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(@NotNull ResultContentHolder rcHolder, BasicTranslationTask task) {
        //设置翻译引擎的样子
        sb.setLength(0);
        if(task.getEngineKind() == Consts.ENGINE_JS){
            TranslationCustom custom = (TranslationCustom)task;
            sb.append(custom.getJSEngine().js.fileName);
        }else{
            sb.append(Consts.ENGINE_NAMES[task.getEngineKind()]);
        }

        sb.append("  ");
        sb.append(Consts.LANGUAGE_NAMES[task.sourceLanguage]);
        sb.append("->");
        sb.append(Consts.LANGUAGE_NAMES[task.targetLanguage]);

        rcHolder.engine.setText(sb.toString());


        if (task.getEngineKind()==Consts.ENGINE_BIGGER_TEXT){//缩小字符
            rcHolder.text.setTextSize(8);
        }else{
            rcHolder.text.setTextSize(16);
        }
        if(task.getResult()==null)return;
        rcHolder.text.setText(task.getResult().getBasicResult());
    }

     static class ResultContentHolder extends BaseViewHolder{
        TextView text,engine;
        ImageButton copyButton,ttsButton;
        public ResultContentHolder(View itemView){
            super(itemView);
            text=itemView.findViewById(R.id.view_result_content_text);
            engine=itemView.findViewById(R.id.view_result_content_engine);
            copyButton=itemView.findViewById(R.id.view_result_content_copy_button);
            ttsButton=itemView.findViewById(R.id.view_result_content_speak_button);
        }
    }
}
