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
public class ResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
	String[][] results;
	ArrayList<TranslationTask> tasks;
	Context ctx;

	TranslationTask task;
	StringBuilder sb=new StringBuilder();
	public ResultAdapter(Context ctx,ArrayList<TranslationTask> tasks)
	{
		this.tasks = tasks;
		this.ctx = ctx;
	}
	
	public void updata(ArrayList<TranslationTask> tasks){
		this.tasks=tasks;
		notifyDataSetChanged();
	}
	
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int position)
	{
		// TODO: Implement this method
		View view=LayoutInflater.from(ctx).inflate(R.layout.view_result_content,null);
		ResultContentHolder holder=new ResultContentHolder(view);
		return holder;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int i)
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
			rcHolder.copyButton.setClickable(true);
			rcHolder.copyButton.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View p1)
					{
						// TODO: Implement this method
						TextView tv=rcHolder.text;
						String text=tv.getText().toString();
						System.out.println(text);
						ApplicationUtil.copyToClipboard(ctx,text);
						ApplicationUtil.print(ctx,"已复制翻译结果["+(text.length()<8?text:(text.substring(0,5))+"...")+"]到剪贴板！");
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
		return super.getItemViewType(position);
	}
	
	@Override
	public int getItemCount()
	{
		// TODO: Implement this method
		return tasks==null?0:tasks.size();
	}
	
	class ResultContentHolder extends RecyclerView.ViewHolder{
		TextView text,engine;
		Button copyButton;
		public ResultContentHolder(View itemView){
			super(itemView);
			text=itemView.findViewById(R.id.view_result_content_text);
			engine=itemView.findViewById(R.id.view_result_content_engine);
			copyButton=itemView.findViewById(R.id.view_result_content_copy_button);
		}
		
	}
	
	
}
