package com.funny.translation.widget;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.funny.translation.R;
import com.funny.translation.bean.LanguageBean;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import java.util.ArrayList;
import com.funny.translation.bean.Consts;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.graphics.Color;
public class LanguageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
	Context ctx;
	public ArrayList<LanguageBean> list;
	public ArrayList<Integer> selectedList=new ArrayList<Integer>();
	public short kind;
	private OnClickItemListener listener;
	private RecyclerView rv;
	public LanguageAdapter(Context ctx,final ArrayList<LanguageBean> list,short kind,RecyclerView rv)
	{
		this.ctx = ctx;
		this.list = list;
		this.kind=kind;
		this.rv=rv;
	}
	
	public void setOnClickItemListener(OnClickItemListener listener){
		if(listener!=null){
			this.listener=listener;
		}
	}
	
	private boolean isNoneChecked(){//判断是否全部都没有选
		for(LanguageBean bean:list){
			if(bean.isSelected()){
				return false;
			}
		}
		return true;
	}
	
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parentView, int i)
	{
		// TODO: Implement this method
		View v=LayoutInflater.from(ctx).inflate(R.layout.view_language_item,parentView,false);
		LanguageViewHolder holder=new LanguageViewHolder(v);
		return holder;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder p1,final int i)
	{
		// TODO: Implement this method
		Drawable drawable;
		if(p1 instanceof LanguageViewHolder){
			LanguageViewHolder holder=(LanguageViewHolder)p1;
			LanguageBean bean=list.get(i);
			holder.tv.setText(bean.text);
			holder.tv.setClickable(true);
			holder.tv.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View p1)
					{
						// TODO: Implement this method
						//listener.onClick(i);
						//Toast.makeText(ctx,"点击",Toast.LENGTH_SHORT).show();
						switch(kind){
							case Consts.CHECK_MULTI:
								LanguageBean bean=list.get(i);
								bean.setIsSelected(!bean.isSelected());
								if(isNoneChecked()){//都没选，不执行
									bean.setIsSelected(true);
									return;
								}
								//print("isSelected:"+bean.isSelected());
								break;
							case Consts.CHECK_SINGLE:
								for(LanguageBean curBean:list){
									curBean.setIsSelected(false);
								}
								LanguageBean bean2=list.get(i);
								bean2.setIsSelected(!bean2.isSelected());
								break;
						}
						notifyDataSetChanged();
					}
			});
			if(bean.getCheckKind()==Consts.CHECK_SINGLE){
				drawable=bean.isSelected()?Consts.IC_SINGLE_CHECK_CHECKED:Consts.IC_SINGLE_CHECK;
			}else{
				drawable=bean.isSelected()?Consts.IC_MULTI_CHECK_CHECKED:Consts.IC_MULTI_CHECK;
			}
			
			if(bean.isSelected()){
				holder.tv.setBackgroundColor(Color.parseColor("#ffcc80"));
			}else{
				holder.tv.setBackground(null);
			}
			
			//System.out.println("drawable size:"+drawable.getMinimumWidth());
			drawable.setBounds(0,0,80,80);
			holder.tv.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable,null,null,null);
		}
	}

	@Override
	public int getItemCount()
	{
		// TODO: Implement this method
		return list==null?0:list.size();
	}
	
	private class LanguageViewHolder extends RecyclerView.ViewHolder{
		TextView tv;
		public LanguageViewHolder(View view){
			super(view);
			tv=view.findViewById(R.id.view_language_tv);
		}
		
	}
	
	public interface OnClickItemListener{
		public void onClick(int position)
	}
}
