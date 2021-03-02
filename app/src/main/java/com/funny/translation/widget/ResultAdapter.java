package com.funny.translation.widget;
import android.annotation.SuppressLint;
import android.view.ViewGroup;

import com.funny.translation.js.TranslationCustom;
import com.funny.translation.translation.BasicTranslationTask;

import java.util.ArrayList;

import android.view.View;
import android.widget.TextView;
import com.funny.translation.R;
import android.view.LayoutInflater;

import com.funny.translation.bean.Consts;

import android.view.View.OnClickListener;
import com.funny.translation.utils.ApplicationUtil;
import com.funny.translation.utils.TTSUtil;
import com.funny.translation.MainActivity;
import android.widget.Space;
import android.widget.ImageButton;

import androidx.recyclerview.widget.RecyclerView;

public class ResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
	String[][] results;
	ArrayList<BasicTranslationTask> tasks;
	MainActivity ctx;

	BasicTranslationTask task;
	StringBuilder sb=new StringBuilder();
	
	int curPosition=0;
	public ResultAdapter(MainActivity ctx,ArrayList<BasicTranslationTask> tasks)
	{
		this.tasks = tasks;
		this.ctx = ctx;
	}
	
	public void update(ArrayList<BasicTranslationTask> tasks){
		this.tasks=tasks;
		notifyDataSetChanged();
	}
	
	public void insert(ArrayList<BasicTranslationTask> tasks){
		this.tasks=tasks;
		if(tasks.size()==0){
			return;
		}else if(tasks.size()==1){
			notifyDataSetChanged();
			return;
		}
		notifyItemInserted(tasks.size()-1);
	}
	
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type)
	{
		// TODO: Implement this method
		if(type==0){
			View view=LayoutInflater.from(ctx).inflate(R.layout.view_result_content,parent,false);
			ResultContentHolder holder=new ResultContentHolder(view);
			return holder;
		}else{
			View view=LayoutInflater.from(ctx).inflate(R.layout.view_result_space,parent,false);
			SpaceViewHolder holder=new SpaceViewHolder(view);
			return holder;
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int i)
	{
		// TODO: Implement this method
		if(holder instanceof ResultContentHolder){
			final ResultContentHolder rcHolder=(ResultContentHolder)holder;
			task=tasks.get(i);
			sb.setLength(0);
			if(task.engineKind==Consts.ENGINE_JS){
				TranslationCustom custom = (TranslationCustom)task;
				sb.append(custom.getJSEngine().js.fileName);
			}else{
				sb.append(Consts.ENGINE_NAMES[task.engineKind]);
			}

			sb.append("  ");
			sb.append(Consts.LANGUAGE_NAMES[task.sourceLanguage]);
			sb.append("->");
			sb.append(Consts.LANGUAGE_NAMES[task.targetLanguage]);
			rcHolder.engine.setText(sb.toString());

			if (task.engineKind==Consts.ENGINE_BIGGER_TEXT){//缩小字符
				rcHolder.text.setTextSize(8);
			}else{
				rcHolder.text.setTextSize(16);
			}
			if(task.getResult()==null)return;
			rcHolder.text.setText(task.getResult().getBasicResult());
			//System.out.println("resultString:"+task.resultString);
			if (!rcHolder.copyButton.hasOnClickListeners()) {
				rcHolder.copyButton.setClickable(true);
				rcHolder.copyButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View p1) {
						// TODO: Implement this method
						TextView tv = rcHolder.text;
						String text = tv.getText().toString();
						//System.out.println(text);
						ApplicationUtil.copyToClipboard(ctx, text);
						ApplicationUtil.print(ctx, "已复制翻译结果[" + (text.length() < 8 ? text : (text.substring(0, 5)) + "...") + "]到剪贴板！");
					}
				});
			}
			if (!rcHolder.ttsButton.hasOnClickListeners()) {
				rcHolder.ttsButton.setClickable(true);
				rcHolder.ttsButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View p1) {
						// TODO: Implement this method
						BasicTranslationTask curTask = tasks.get(i);
						short engineKind = curTask.engineKind;
						if (engineKind == Consts.ENGINE_BV_TO_AV || engineKind == Consts.ENGINE_BIGGER_TEXT || engineKind == Consts.ENGINE_EACH_TEXT) {
							ApplicationUtil.print(ctx, "当前引擎的翻译结果不支持朗读哦~");
							return;
						}
						short TTSEngine = ctx.getCheckedTTSEngine();
						System.out.println("TTSEngine:" + TTSEngine);
						short targetLanguage = curTask.targetLanguage;
						if (targetLanguage == Consts.LANGUAGE_WENYANWEN) {
							targetLanguage = Consts.LANGUAGE_CHINESE;
						}
						TTSUtil.speak(ctx, curTask.getResult().getBasicResult(), targetLanguage, TTSEngine);
					}
				});
			}
			//rcHolder.engine.setText(results[i][1]);
			//rcHolder.text.setText(results[i][0]);
		}
	}

	@Override
	public int getItemViewType(int position)
	
	{
		// TODO: Implement this method
		if(position==tasks.size()){//点击的最后一个
			return 1;
		}else{
			return 0;
		}
	}
	
	@Override
	public int getItemCount()
	{
		// TODO: Implement this method
		return tasks==null?0:(tasks.size()+1);
	}

	public ArrayList<BasicTranslationTask> getTasks() {
		return tasks;
	}

	public void setTasks(ArrayList<BasicTranslationTask> tasks) {
		this.tasks = tasks;
	}

	class ResultContentHolder extends RecyclerView.ViewHolder{
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
	
	private class SpaceViewHolder extends RecyclerView.ViewHolder{
		Space space;
		public SpaceViewHolder(View view){
			super(view);
			space=view.findViewById(R.id.view_result_space_space);
			//space=new Space(ctx);
			//ViewGroup.LayoutParams param=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,8);
			//space.setLayoutParams(param);
		}
	}
	
}
