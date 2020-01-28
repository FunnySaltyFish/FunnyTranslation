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
//import com.qw.soul.permission.SoulPermission;
import android.Manifest;
//import com.qw.soul.permission.callbcak.CheckRequestPermissionListener;
//import com.qw.soul.permission.bean.Permission;
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
import static com.funny.translation.bean.Consts.*;
import android.view.ViewGroup;
import com.billy.android.swipe.consumer.StretchConsumer;
import com.billy.android.swipe.SwipeConsumer;
//import com.luwei.checkhelper.MultiCheckHelper;
//import com.luwei.checkhelper.CheckHelper;
import com.funny.translation.widget.LanguageAdapter;
import com.funny.translation.bean.LanguageBean;
import android.content.res.Resources;
import com.funny.translation.utils.BitmapUtil;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import com.funny.translation.widget.LanguageAdapter.OnClickItemListener;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.Color;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.text.TextWatcher;
import android.text.Editable;
import com.funny.translation.utils.ApplicationUtil;
public class MainActivity extends BaseActivity 
{
	Resources re;
	
	Toolbar toolbar;
	EditText inputText;
	RecyclerView outputRecyclerView;
	RecyclerView.LayoutManager layoutManager;
	ResultItemDecoration itemDecoration;
	Button translateButton;
	
	ArrayList<LanguageBean> sourceList,targetList,engineList;
	
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
		re=getResources();
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
							if(!translateButton.isEnabled()){//当处于暂停状态时，恢复
								translateButton.setEnabled(true);
							}
							translatingProgressDialog.dismiss();
						}
						//ArrayList<TranslationTask> task
						//outputText.setText(helper.tasks.get(0).resultString);
						break;
				}
			}
		};
		initConsts();
		initMainView();
		createSlideView();
		initBitmaps();
		createDialogs();
    }
	
	private void initConsts(){
		Consts.LANGUAGE_NAMES=re.getStringArray(R.array.languages);
	}

	private void initMainView()
	{
		toolbar = findViewById(R.id.widget_main_toolbar);
		setSupportActionBar(toolbar);

		inputText = findViewById(R.id.widget_main_inputtext);
		inputText.addTextChangedListener(new TextWatcher(){
				@Override
				public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4)
				{
					// TODO: Implement this method
				}

				@Override
				public void onTextChanged(CharSequence p1, int p2, int p3, int p4)
				{
					// TODO: Implement this method
					if(p1!=null&&p1.length()>0){
						translateButton.setEnabled(true);
					}else{
						translateButton.setEnabled(false);
					}
				}

				@Override
				public void afterTextChanged(Editable p1)
				{
					// TODO: Implement this method
				}
		});
		outputRecyclerView = findViewById(R.id.widget_main_recycler_view);

		adapter = new ResultAdapter(this, null);
		itemDecoration = new ResultItemDecoration(this, tasks);
		layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
		outputRecyclerView.setAdapter(adapter);
		outputRecyclerView.setLayoutManager(layoutManager);
		outputRecyclerView.addItemDecoration(itemDecoration);

		translateButton = findViewById(R.id.widget_main_translate);
		translateButton.setClickable(true);
		translateButton.setEnabled(false);
		translateButton.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view)
				{
					// TODO: Implement this method
					helper = new TranslationHelper(MainActivity.this.handler);
					initTasks();
					helper.setTasks(tasks);
					helper.totalTimes = tasks.size();
					helper.start();
					dialogTranslatingProgressbar.setMax(tasks.size());
					dialogTranslatingProgressbar.setProgress(0);
					showTranslatingDialog();
				}
			});
	}
	
	private void createSlideView(){
//		View leftSlideView=LayoutInflater.from(this).inflate(R.layout.main_slide_right,null);
//		leftSlideView.setLayoutParams(new ViewGroup.LayoutParams(SmartSwipe.dp2px(280,this), ViewGroup.LayoutParams.MATCH_PARENT));
//		//SmartSwipeWrapper leftMenuWrapper = SmartSwipe.wrap(leftSlideView).addConsumer(new StretchConsumer()).enableVertical().getWrapper();
//		DrawerConsumer leftDrawerConsumer=new DrawerConsumer().setDrawerView(SwipeConsumer.DIRECTION_LEFT,leftSlideView);
//		leftDrawerConsumer.setEdgeSize(SmartSwipe.dp2px(40,this));
//		SmartSwipe.wrap(this).addConsumer(leftDrawerConsumer);
		
		View rightSlideView=LayoutInflater.from(this).inflate(R.layout.main_slide_right,null);
		RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(SmartSwipe.dp2px(180,this), ViewGroup.LayoutParams.MATCH_PARENT);
		//params.setMargins(4,getStatusBarHeight()+100,4,4);
		rightSlideView.setLayoutParams(params);
		
		TextView tv=rightSlideView.findViewById(R.id.main_slide_right_source_tv);
		RelativeLayout.LayoutParams tvParam=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		tvParam.setMargins(0,getStatusBarHeight(),0,4);
		tv.setLayoutParams(tvParam);
		
		RecyclerView rightSourceRv=rightSlideView.findViewById(R.id.main_slide_right_source_rv);
		sourceList=new ArrayList<LanguageBean>();
		for(short i=0;i<Consts.LANGUAGE_NAMES.length;i++){
			LanguageBean bean=new LanguageBean();
			bean.setIsSelected(false);
			bean.setCheckKind(Consts.CHECK_SINGLE);
			bean.setUserData(i);
			bean.setText(LANGUAGE_NAMES[i]);
			sourceList.add(bean);
		}
		
		RecyclerView rightTargetRv=rightSlideView.findViewById(R.id.main_slide_right_target_rv);
		targetList=new ArrayList<LanguageBean>();
		for(short i=0;i<Consts.LANGUAGE_NAMES.length;i++){
			LanguageBean bean=new LanguageBean();
			bean.setIsSelected(false);
			bean.setCheckKind(Consts.CHECK_MULTI);
			bean.setUserData(i);
			bean.setText(LANGUAGE_NAMES[i]);
			targetList.add(bean);
		}
		
		RecyclerView rightEngineRv=rightSlideView.findViewById(R.id.main_slide_right_engine_rv);
		engineList=new ArrayList<LanguageBean>();
		for(short i=0;i<Consts.ENGINE_NAMES.length;i++){
			LanguageBean bean=new LanguageBean();
			bean.setIsSelected(false);
			bean.setUserData(i);
			bean.setCheckKind(Consts.CHECK_MULTI);
			bean.setText(ENGINE_NAMES[i]);
			engineList.add(bean);
		}
		
		//默认勾选
		sourceList.get(Consts.LANGUAGE_CHINESE).setIsSelected(true);
		targetList.get(Consts.LANGUAGE_ENGLISH).setIsSelected(true);
		engineList.get(Consts.ENGINE_YOUDAO_NORMAL).setIsSelected(true);
		engineList.get(Consts.ENGINE_BAIDU_NORMAL).setIsSelected(true);
		
		final LanguageAdapter targetAdapter=new LanguageAdapter(this,targetList,Consts.CHECK_MULTI,rightTargetRv);
		final LanguageAdapter sourceAdapter=new LanguageAdapter(this,sourceList,Consts.CHECK_SINGLE,rightSourceRv);
		final LanguageAdapter engineAdapter=new LanguageAdapter(this,engineList,Consts.CHECK_MULTI,rightEngineRv);
		
		LinearLayoutManager linearLM1=new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
		rightSourceRv.setAdapter(sourceAdapter);
		rightSourceRv.setClickable(true);
		rightSourceRv.setLayoutManager(linearLM1);
		
		LinearLayoutManager linearLM2=new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
		rightTargetRv.setAdapter(targetAdapter);
		rightTargetRv.setClickable(true);
		rightTargetRv.setLayoutManager(linearLM2);
		
		LinearLayoutManager linearLM3=new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
		rightEngineRv.setAdapter(engineAdapter);
		rightEngineRv.setClickable(true);
		rightEngineRv.setLayoutManager(linearLM3);
		
		//SmartSwipeWrapper rightMenuWrapper = SmartSwipe.wrap(rightSlideView).addConsumer(new StretchConsumer()).enableVertical().getWrapper();
		DrawerConsumer rightDrawerConsumer=new DrawerConsumer().setDrawerView(SwipeConsumer.DIRECTION_RIGHT,rightSlideView);
		rightDrawerConsumer.setEdgeSize(SmartSwipe.dp2px(40,this));
		rightDrawerConsumer.setShadowColor(Color.parseColor("#9e9e9e"));
		rightDrawerConsumer.setShadowSize(SmartSwipe.dp2px(8,this));
		SmartSwipe.wrap(this).addConsumer(rightDrawerConsumer);
	}
	
	private void initTasks(){
		if(tasks==null){
			tasks=new ArrayList<TranslationTask>();
		}else{
			tasks.clear();
		}
		short source=getCheckedList(sourceList).get(0).getUserData();
		int i=0;
		for(LanguageBean target:getCheckedList(targetList)){
			for(LanguageBean engine:getCheckedList(engineList)){
				TranslationTask task=new TranslationTask(engine.getUserData(),source,target.getUserData(),inputText.getText().toString());
				task.setId(i++);
				task.setLisener(helper.defaultListener);
				tasks.add(task);
			}
		}
//		for(int i=0;i<2;i++){
//			TranslationTask task=new TranslationTask(ENGINE_YOUDAO_NORMAL,LANGUAGE_ENGLISH,LANGUAGE_CHINESE,inputText.getText().toString());
//			task.setId(i);
//			task.setLisener(helper.defaultListener);
//			tasks.add(task);
//		}
		
	}
	
	private ArrayList<LanguageBean> getCheckedList(ArrayList<LanguageBean> list){
		ArrayList<LanguageBean> result=new ArrayList<LanguageBean>();
		for(LanguageBean bean:list){
			if(bean.isSelected()){
				result.add(bean);
			}
		}
		return result;
	}
	
	private void initBitmaps(){
		//IC_SINGLE_CHECK_CHECKED=BitmapUtil.getBitmapFromResources();
		IC_SINGLE_CHECK_CHECKED=getIcon(R.drawable.ic_single_check_checked);
		IC_SINGLE_CHECK=getIcon(R.drawable.ic_single_check);
		IC_MULTI_CHECK=getIcon(R.drawable.ic_multi_check);
		IC_MULTI_CHECK_CHECKED=getIcon(R.drawable.ic_multi_check_checked);
	}
	
	private Drawable getIcon(int id){
		Bitmap bitmap=BitmapUtil.getBigBitmapFromResources(re,id,32,32);
		BitmapDrawable b=new BitmapDrawable(bitmap);
		b.setColorFilter(Color.parseColor("#2196f3"),PorterDuff.Mode.SRC_IN);
		return (Drawable)b;
	};
	
	private int getStatusBarHeight() {
		int resourceId = re.getIdentifier("status_bar_height", "dimen","android");
		int height = re.getDimensionPixelSize(resourceId);
		//print("statuebarh:"+height);
		return height;
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
					helper.flag=0;
					ApplicationUtil.print(MainActivity.this,"已停止");
					adapter.notifyDataSetChanged();
				}
			})
			.setNegativeButton("后台", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					// TODO: Implement this method
					translateButton.setEnabled(false);
					ApplicationUtil.print(MainActivity.this,"任务已转至后台，完成前请不要开始新任务！");
				}
			})
			.create();
	}
	
	public void showTranslatingDialog(){
		if(translatingProgressDialog!=null){
			translatingProgressDialog.show();
		}
	}
	
//	private void getInternetPermission(){
//		SoulPermission.getInstance().checkAndRequestPermission(Manifest.permission.ACCESS_NETWORK_STATE, new CheckRequestPermissionListener(){
//				@Override
//				public void onPermissionOk(Permission p)
//				{
//					// TODO: Implement this method
//					print("已授权");
//				}
//
//				@Override
//				public void onPermissionDenied(Permission p)
//				{
//					// TODO: Implement this method
//					print("授权失败！");
//				}
//		});
//	}
	
}
