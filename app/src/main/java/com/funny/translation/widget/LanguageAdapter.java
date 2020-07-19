package com.funny.translation.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.funny.translation.R;
import com.funny.translation.bean.Consts;
import com.funny.translation.bean.DoubleMap;
import com.funny.translation.bean.LanguageBean;

import java.util.ArrayList;

public class LanguageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
	Context ctx;
	public ArrayList<LanguageBean> list;
	public ArrayList<Integer> selectedList=new ArrayList<Integer>();
	public short kind;
	private OnClickItemListener listener;
	private RecyclerView rv;


	//private DoubleMap doubleMap;
	private int[] mapping;//通过映射关系完成自定义顺序

	String TAG = "LanguageAdapter";
	public LanguageAdapter(Context ctx, ArrayList list, short kind, RecyclerView rv,int[] mapping)
	{
		this.ctx = ctx;
		this.list = list;
		this.kind=kind;
		this.rv=rv;
		this.mapping = mapping;
		//doubleMap = new DoubleMap(mapping);
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
	public void onBindViewHolder(RecyclerView.ViewHolder p1, @SuppressLint("RecyclerView") final int i)
	{
		// TODO: Implement this method
		Drawable drawable;
		if(p1 instanceof LanguageViewHolder){
			LanguageViewHolder holder=(LanguageViewHolder)p1;
			LanguageBean bean = list.get(mapping[i]);
			//LanguageBean bean=list.get(doubleMap.getByKey(i));
			//Log.i(TAG,String.format("i is %d , adn getByKey returns %d",i,doubleMap.getByKey(i)));
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
								int actualIndex = getMappingIndex(i);
								LanguageBean bean=list.get(actualIndex);
								Log.i(TAG,String.format("i is %d , adn getByValue returns %d",i,getMappingIndex(i)));
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
								Log.i(TAG,String.format("i is %d , adn getByValue returns %d",i,getMappingIndex(i)));
								int  actualIndex2 = getMappingIndex(i);
								LanguageBean bean2=list.get(actualIndex2);
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
		//加一用于防止最后一个控件显示不全
	}

//	public void setMapping(int[] ints) {
//		doubleMap.set(ints);
//	}

	private class LanguageViewHolder extends RecyclerView.ViewHolder{
		TextView tv;
		public LanguageViewHolder(View view){
			super(view);
			tv=view.findViewById(R.id.view_language_tv);
		}
	}
	
	private int getMappingIndex(int number){
		//return doubleMap.getByKey(number);
		return mapping[number];
	}
	public interface OnClickItemListener {
		public void onClick(int position);
	}
}
