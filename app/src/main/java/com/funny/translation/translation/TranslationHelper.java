package com.funny.translation.translation;

import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.io.InputStreamReader;
import java.io.IOException;
import com.funny.translation.bean.Consts;
import android.os.Handler;
import android.os.Message;
import java.util.Map;
import java.util.List;

public class TranslationHelper extends Thread
{
	public String sourceString;
	public String resultString;
	public short engineKind=0;
	public ArrayList<TranslationTask> tasks,finishTasks;
	
	public int successTimes=0,failureTimes=0;
	public int totalTimes=0;
	
	public int flag=0;
	private int curI=0;//当前翻译的是第几个
	
	private Handler handler;
	
	public final static short FLAG_TRANSLATING=1;
	public final static short FLAG_FINISHED=2;
	
	public OnTranslateListener defaultListener;
	public TranslationHelper(Handler handler){
		this.handler=handler;
		this.defaultListener=new OnTranslateListener(){
			@Override
			public void onSuccess(String source,String result)
			{
				// TODO: Implement this method
				System.out.printf("成功！%s的翻译结果是：%s\n",source,result);
				successTimes++;
			}

			@Override
			public void onFail(String reason)
			{
				// TODO: Implement this method
				System.out.printf("失败！原因是：\n%s",reason);
				failureTimes++;
			}
		};
		finishTasks=new ArrayList<TranslationTask>();
	}

	public void setTasks(ArrayList<TranslationTask> tasks)
	{
		// TODO: Implement this method
		this.tasks=tasks;
	}
	
//	public void startTasks(){
//		if(this.tasks==null||this.tasks.isEmpty()){return;}
//		TranslationTask task;
//		for(int i=0;i<this.tasks.size();i++){
//			task=this.tasks.get(i);
//			task.translate();
//		}
//	}
	
	public int getProcess(){
		int progress=Math.round((float)(this.failureTimes+this.successTimes)/(float)this.totalTimes*100);
		return progress;
	}
	
	@Override
	public void run()
	{
		// TODO: Implement this method
		super.run();
		while(flag>0){
			if(flag==FLAG_TRANSLATING){
				if(this.tasks==null||this.tasks.isEmpty()){return;}
				TranslationTask task=this.tasks.get(curI++);
				task.translate();
				try
				{
					finishTasks.add(task);
					Message msg=new Message();
					msg.what=0x001;
					msg.obj=1;
					handler.sendMessage(msg);
					//System.out.println("我是线程，我在执行translating");
					if(task.engineKind==Consts.ENGINE_BAIDU_NORMAL){
						sleep(800);
					}else{
						sleep(50);
					}
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				if (curI == this.tasks.size()){
					flag=FLAG_FINISHED;
				}
			}else if(flag==FLAG_FINISHED){
				Message msg=new Message();
				msg.obj=this.tasks;
				msg.what=0x002;
				handler.sendMessage(msg);
				flag=0;
				curI=0;
			}
		}
	}
	
//	public String[][] getResult(){
//		if(this.tasks==null||this.tasks.isEmpty()){return null;}
//		String[][] resultStrs=new String[this.tasks.size()][2];
//		TranslationTask task;
//		for(int i=0;i<this.tasks.size();i++){
//			task=this.tasks.get(i);
//			System.out.println("resultStrs:"+resultStrs.toString()+"  length:"+resultStrs.length);
//			resultStrs[i][0]=task.resultString;
//			resultStrs[i][1]=Consts.ENGINE_NAMES[task.engineKind];
//		}
//		return resultStrs;
//	}
//	
	public void reStart(){
		this.flag = FLAG_TRANSLATING;
		this.curI = 0;
		//finishTasks.clear();
	}
	

	@Override
	public void start()
	{
		// TODO: Implement this method
		super.start();
		reStart();
	}
	
}
