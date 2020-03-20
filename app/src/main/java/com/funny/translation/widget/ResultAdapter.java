package com.funny.translation.widget;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import com.funny.translation.translation.TranslationTask;
import java.util.ArrayList;
import java.util.List;
import android.view.View;
import android.widget.TextView;
import com.funny.translation.R;
import android.view.LayoutInflater;
import android.content.Context;
import com.funny.translation.bean.Consts;
import android.widget.Button;
import android.view.View.OnClickListener;
import com.funny.translation.utils.ApplicationUtil;
import android.support.v7.widget.AppCompatButton;
import com.funny.translation.utils.TTSUtil;
import com.funny.translation.MainActivity;
import android.widget.Space;
import android.widget.ImageButton;
public class ResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
	String[][] results;
	ArrayList<TranslationTask> tasks;
	MainActivity ctx;
	
	TranslationTask task;
	StringBuilder sb=new StringBuilder();
	
	int curPosition=0;
	public ResultAdapter(MainActivity ctx,ArrayList<TranslationTask> tasks)
	{
		this.tasks = tasks;
		this.ctx = ctx;
	}
	
	public void updata(ArrayList<TranslationTask> tasks){
		this.tasks=tasks;
		notifyDataSetChanged();
	}
	
	public void insert(ArrayList<TranslationTask> tasks){
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
	public void onBindViewHolder(RecyclerView.ViewHolder holder, final int i)
	{
		// TODO: Implement this method
		if(holder instanceof ResultContentHolder){
			final ResultContentHolder rcHolder=(ResultContentHolder)holder;
			task=tasks.get(i);
			sb.setLength(0);
			sb.append(Consts.ENGINE_NAMES[task.engineKind]);
			sb.append("  ");
			sb.append(Consts.LANGUAGE_NAMES[task.sourceLanguage]);
			sb.append("->");
			sb.append(Consts.LANGUAGE_NAMES[task.targetLanguage]);
			rcHolder.engine.setText(sb.toString());
			rcHolder.text.setText(task.resultString);
			//System.out.println("resultString:"+task.resultString);
			rcHolder.copyButton.setClickable(true);
			rcHolder.copyButton.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View p1)
					{
						// TODO: Implement this method
						TextView tv=rcHolder.text;
						String text=tv.getText().toString();
						//System.out.println(text);
						ApplicationUtil.copyToClipboard(ctx,text);
						ApplicationUtil.print(ctx,"已复制翻译结果["+(text.length()<8?text:(text.substring(0,5))+"...")+"]到剪贴板！");
					}
			});
			rcHolder.ttsButton.setClickable(true);
			rcHolder.ttsButton.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View p1)
					{
						// TODO: Implement this method
						short TTSEngine=ctx.getCheckedTTSEngine();
						System.out.println("TTSEngine:"+TTSEngine);
//						if(TTSEngine==Consts.TTS_LOCAL&&TTSUtil.localTTS==null){
//							TTSUtil.initLocal(ctx);
//						}else{
//							TTSUtil.initInternet(ctx);
//						}
						//System.out.println("id:"+rcHolder.getId());
						TranslationTask curTask=tasks.get(i);
						TTSUtil.speak(ctx,curTask.resultString,curTask.targetLanguage,TTSEngine);
					}
			});
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
