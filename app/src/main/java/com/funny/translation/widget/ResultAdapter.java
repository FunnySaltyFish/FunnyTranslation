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
public class ResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
	String[][] results;
	ArrayList<TranslationTask> tasks;
	Context ctx;

	TranslationTask task;
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
			ResultContentHolder rcHolder=(ResultContentHolder)holder;
			task=tasks.get(i);
			rcHolder.engine.setText(Consts.ENGINE_NAMES[task.engineKind]);
			rcHolder.text.setText(task.resultString);
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
		public ResultContentHolder(View itemView){
			super(itemView);
			text=itemView.findViewById(R.id.view_result_content_text);
			engine=itemView.findViewById(R.id.view_result_content_engine);
		}
		
	}
	
	
}
