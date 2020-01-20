package com.funny.translation.widget;
import android.graphics.*;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import com.funny.translation.translation.TranslationTask;
import java.util.ArrayList;

public class ResultItemDecoration extends RecyclerView.ItemDecoration
{
	/*
	 参考
	 https://blog.csdn.net/zxt0601/article/details/52355199
	 */
	private ArrayList<TranslationTask> mTasks;
	private Paint mPaint;
	private Rect mBounds;//用于存放测量文字Rect
	
	private int mContentDivision;
	private int mContentLeftMargin;

	private Context context;
	public ResultItemDecoration(Context ctx,ArrayList<TranslationTask> tasks){
		this.context=ctx;
		this.mTasks = tasks;
		mPaint = new Paint();
		mBounds = new Rect();
		mContentDivision=(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, context.getResources().getDisplayMetrics());
		mContentLeftMargin=(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, context.getResources().getDisplayMetrics());
	}

	public void setTasks(ArrayList<TranslationTask> tasks){
		this.mTasks=tasks;
	}

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
		super.getItemOffsets(outRect, view, parent, state);
		outRect.set(mContentLeftMargin, mContentDivision, mContentLeftMargin, 0);
	}
}

