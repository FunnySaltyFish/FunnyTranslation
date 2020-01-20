package com.funny.translation;

import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.view.View;
import com.funny.translation.translation.TranslationHelper;
import com.funny.translation.translation.TranslationTask;
import com.funny.translation.bean.Consts;
import android.support.v7.app.AlertDialog;
import com.qw.soul.permission.SoulPermission;
import android.Manifest;
import com.qw.soul.permission.callbcak.CheckRequestPermissionListener;
import com.qw.soul.permission.bean.Permission;
import android.widget.Toast;
import android.os.Handler;
import android.os.Message;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.widget.ProgressBar;
import com.billy.android.swipe.SmartSwipeWrapper;
import com.billy.android.swipe.SmartSwipe;
import com.billy.android.swipe.consumer.DrawerConsumer;
import android.support.v7.widget.RecyclerView;
import com.funny.translation.widget.ResultAdapter;
import android.support.v7.widget.LinearLayoutManager;
import com.funny.translation.widget.ResultItemDecoration;
import java.util.ArrayList;

public class MainActivity extends BaseActivity 
{
	Toolbar toolbar;
	EditText inputText;
	RecyclerView outputRecyclerView;
	RecyclerView.LayoutManager layoutManager;
	ResultItemDecoration itemDecoration;
	Button translateButton;
	
	SmartSwipeWrapper swipeWrapper;
	
	String[][] translationResult;
	ArrayList<TranslationTask> tasks;
	ResultAdapter adapter;
	TranslationHelper helper;
	
	Handler handler;
	
	AlertDialog webPermissionDialog=null;
	AlertDialog translatingProgressDialog=null;
	TextView dialogTranslatingContentTV;
	ProgressBar dialogTranslatingProgressbar;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		setTheme(R.style.AppTheme_NoActionBar);
		setContentView(R.layout.main);
		handler=new Handler(){
			@Override
			public void handleMessage(Message msg){
				switch(msg.what){
					case 0x001:
						Object obj=msg.obj;
						if(obj!=null){
							dialogTranslatingProgressbar.setProgress(helper.getProcess());
							dialogTranslatingContentTV.setText("已完成："+helper.getProcess()+"/"+helper.totalTimes+"");
						}
						break;
					case 0x002:
						Object obj2=msg.obj;
						if(obj2!=null){
							//translationResult=(String[][])obj2;
							//adapter.updata(translationResult);
							tasks=(ArrayList<TranslationTask>)obj2;
							adapter.updata(tasks);
							itemDecoration.setTasks(tasks);
							outputRecyclerView.invalidateItemDecorations();
							//System.out.println("MainActivity:resultStrs:"+translationResult.toString()+"  length:"+translationResult.length);
							//outputText.setText(translationResult[0][0]);
							translatingProgressDialog.dismiss();
						}
						//ArrayList<TranslationTask> task
						//outputText.setText(helper.tasks.get(0).resultString);
						break;
				}
			}
		};
		
		toolbar=findViewById(R.id.widget_main_toolbar);
		setSupportActionBar(toolbar);
		
		inputText=findViewById(R.id.widget_main_inputtext);
		outputRecyclerView=findViewById(R.id.widget_main_recycler_view);
		
		adapter=new ResultAdapter(this,null);
		itemDecoration=new ResultItemDecoration(this,tasks);
		layoutManager=new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
		outputRecyclerView.setAdapter(adapter);
		outputRecyclerView.setLayoutManager(layoutManager);
		outputRecyclerView.addItemDecoration(itemDecoration);
		
		translateButton=findViewById(R.id.widget_main_translate);
		translateButton.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view)
				{
					// TODO: Implement this method
					helper=new TranslationHelper(MainActivity.this.handler);
					initTasks();
					helper.setTasks(tasks);
					helper.totalTimes=tasks.size();
					helper.start();
					dialogTranslatingProgressbar.setMax(tasks.size());
					dialogTranslatingProgressbar.setProgress(0);
					showTranslatingDialog();
				}
		});
		
		/*swipeWrapper=findViewById(R.id.main_swipe_wrapper);
		SmartSwipe.wrap(this)
			.addConsumer(new DrawerConsumer())
			.enableHorizontal();*/
		
		createDialogs();
		//getInternetPermission();
    }
	
	private void initTasks(){
		if(tasks==null){
			tasks=new ArrayList<TranslationTask>();
		}else{
			tasks.clear();
		}
		for(int i=0;i<2;i++){
			TranslationTask task=new TranslationTask(Consts.ENGINE_YOUDAO_NORMAL,inputText.getText().toString());
			task.setId(i);
			task.setLisener(helper.defaultListener);
			tasks.add(task);
		}
		
	}
	
	private void createDialogs(){
		View view=LayoutInflater.from(this).inflate(R.layout.dialog_translating,null);
		dialogTranslatingContentTV=view.findViewById(R.id.dialog_translating_content_tv);
		dialogTranslatingProgressbar=view.findViewById(R.id.dialog_translating_progressbar);
		
		translatingProgressDialog=new AlertDialog.Builder(this)
			.setTitle("翻译中……")
			.setView(view)
			.setPositiveButton("停止", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					// TODO: Implement this method
					print("已停止");
				}
			})
			.setNegativeButton("暂停", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					// TODO: Implement this method
					print("任务已转至后台，完成前请不要开始新任务！");
				}
			})
			.create();
	}
	
	public void showTranslatingDialog(){
		if(translatingProgressDialog!=null){
			translatingProgressDialog.show();
		}
	}
	
	private void getInternetPermission(){
		SoulPermission.getInstance().checkAndRequestPermission(Manifest.permission.ACCESS_NETWORK_STATE, new CheckRequestPermissionListener(){
				@Override
				public void onPermissionOk(Permission p)
				{
					// TODO: Implement this method
					print("已授权");
				}

				@Override
				public void onPermissionDenied(Permission p)
				{
					// TODO: Implement this method
					print("授权失败！");
				}
		});
	}
	
	private void print(String str){
		Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
	}
	
}
