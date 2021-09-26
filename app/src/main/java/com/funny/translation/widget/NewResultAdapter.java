package com.funny.translation.widget;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.funny.translation.R;
import com.funny.translation.bean.Consts;
import com.funny.translation.js.config.JsConfig;
import com.funny.translation.js.core.JsTranslateTask;
import com.funny.translation.trans.CoreTranslationTask;

import org.jetbrains.annotations.NotNull;

public class NewResultAdapter extends BaseQuickAdapter<CoreTranslationTask, NewResultAdapter.ResultContentHolder> {
    private final StringBuilder sb = new StringBuilder();
    public NewResultAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(@NotNull ResultContentHolder rcHolder, CoreTranslationTask task) {
        //设置翻译引擎的样子
        sb.setLength(0);
        if(task.getEngineName() == JsConfig.JS_ENGINE_KIND){
            JsTranslateTask jsTranslateTask = (JsTranslateTask)task;
            sb.append(jsTranslateTask.getJsEngine().getJsBean().getFileName());
        }else{
            sb.append(Consts.ENGINE_NAMES[task.getEngineName()]);
        }

        sb.append("  ");
        sb.append(Consts.LANGUAGE_NAMES[task.getSourceLanguage()]);
        sb.append("->");
        sb.append(Consts.LANGUAGE_NAMES[task.getTargetLanguage()]);

        rcHolder.engine.setText(sb.toString());


        if (task.getEngineName()==Consts.ENGINE_BIGGER_TEXT){//缩小字符
            rcHolder.text.setTextSize(8);
        }else{
            rcHolder.text.setTextSize(16);
        }
        rcHolder.text.setText(task.getResult().getBasicResult().getTrans());
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
