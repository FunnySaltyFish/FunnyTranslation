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
import com.funny.translation.js.TranslationCustom;

import android.os.Handler;
import android.os.Message;
import java.util.Map;
import java.util.List;

public class TranslationHelper extends Thread
{
	public ArrayList<BasicTranslationTask> tasks,finishTasks;
	
	public int successTimes=0,failureTimes=0;
	public int totalTimes=0;
	
	public int flag=0;
	private int curI=0;//当前翻译的是第几个
	private short mode=0;//翻译模式

	private Handler handler;
	
	public final static short FLAG_TRANSLATING=1;
	public final static short FLAG_FINISHED=2;
	
	public OnTranslateListener defaultListener;
	public TranslationHelper(Handler handler){
		this.handler=handler;
		this.defaultListener=new OnTranslateListener(){
			@Override
			public void onSuccess(TranslationHelper helper, TranslationResult result) {
				System.out.printf("成功！%s的翻译结果是：%s\n",result.getSourceString(),result.getBasicResult());
				successTimes++;
			}

			@Override
			public void onFail(TranslationHelper helper, TranslationResult result) {
				System.out.printf("失败！原因是：\n%s",result.getBasicResult());
				failureTimes++;
			}
		};
		finishTasks= new ArrayList<>();
	}

	public void setTasks(ArrayList<BasicTranslationTask> tasks)
	{
		// TODO: Implement this method
		this.tasks=tasks;
	}

	public int getProcess(){
		return Math.round((float)(this.failureTimes+ this.successTimes)/(float) this.totalTimes*100);
	}

	public void setMode(short mode){
		this.mode=mode;
	}
	
	@Override
	public void run()
	{
		// TODO: Implement this method
		super.run();
		while(flag>0){
			if(flag==FLAG_TRANSLATING){
				if(this.tasks==null||this.tasks.isEmpty()){return;}
				BasicTranslationTask task=this.tasks.get(curI++);
				if(task instanceof TranslationCustom){
					TranslationCustom custom = (TranslationCustom) task;
					custom.translate(mode);
				}else task.translate(mode);
				try {
					finishTasks.add(task);
					Message msg=handler.obtainMessage();
					msg.what = Consts.MESSAGE_FINISH_CURRENT_TASK;
					msg.obj = task;
					msg.arg1 = 1;
					handler.sendMessage(msg);
					if (!task.isOffline()) {//离线翻译不休眠
						//System.out.println("我是线程，我在执行translating");
						if (task.engineKind == Consts.ENGINE_BAIDU_NORMAL) {
							sleep(Consts.BAIDU_SLEEP_TIME);
						} else {
							sleep(50);
						}
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
				Message msg=handler.obtainMessage();
				msg.obj=this.tasks;
				msg.what=Consts.MESSAGE_FINISH_ALL_TASKS;
				handler.sendMessage(msg);
				flag=0;
				curI=0;
			}
		}
	}

	public boolean isTranslating(){
		return flag==FLAG_TRANSLATING;
	}

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
