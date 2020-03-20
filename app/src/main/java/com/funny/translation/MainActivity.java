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
import android.view.KeyEvent;
import com.funny.translation.utils.UpdateUtil;
import com.funny.translation.thread.UpdateThread;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.funny.translation.widget.LanguageRecyclerView;
import com.funny.translation.widget.EditTextField;
import com.funny.translation.utils.TTSUtil;
import com.funny.translation.utils.StringUtil;
import com.github.lzyzsd.circleprogress.CircleProgress;
import com.funny.translation.utils.NetworkUtil;

public class MainActivity extends BaseActivity 
{
	Resources re;
	
	Toolbar toolbar;
	EditTextField inputText;
	RecyclerView outputRecyclerView;
	RecyclerView.LayoutManager layoutManager;
	ResultItemDecoration itemDecoration;
	Button translateButton;
	CircleProgress translateProgress;
	
	LanguageRecyclerView rightSourceRv,rightTargetRv;
	
	ArrayList<LanguageBean> sourceList,targetList,engineList,ttsList;
	
	SmartSwipeWrapper swipeWrapper;
	DrawerConsumer rightDrawerConsumer;//右侧侧滑
	
	String[][] translationResult;
	ArrayList<TranslationTask> tasks;
	ResultAdapter adapter;
	TranslationHelper helper;
	
	Handler handler;
	long curBackTime=0,firstBackTime=0;
	
	AlertDialog webPermissionDialog=null;
	AlertDialog translatingProgressDialog=null;
	AlertDialog tipDialog=null;
	TextView dialogTranslatingContentTV;
	ProgressBar dialogTranslatingProgressbar;
	
	boolean checkdNewVersion=false;//是否检测过更新
	boolean isBackground=false;//Activity是否处于后台
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		
		setTheme(R.style.AppTheme_NoActionBar);
		re=getResources();
		setContentView(R.layout.main);
		
		new Thread(new Runnable(){
				@Override
				public void run()
				{
					initConsts();
					initBitmaps();
					// TODO: Implement this method
				}
		}).start();
		initMainView();
		createSlideView();
		createDialogs();
		initHandler();
		
		new UpdateThread(this).start();
    }

	private void initHandler()
	{
		handler = new Handler(Looper.getMainLooper()){
			@Override
			public void handleMessage(Message msg)
			{
				switch (msg.what)
				{
					case 0x001:
						Object obj=msg.obj;
						if (obj != null)
						{
							//dialogTranslatingProgressbar.setProgress(helper.getProcess());
							//dialogTranslatingContentTV.setText("已完成：" + helper.getProcess() + "/" + helper.totalTimes + "");
							
							adapter.insert(helper.finishTasks);
							translateProgress.setProgress(helper.getProcess());
						}
						break;
					case 0x002:
						Object obj2=msg.obj;
						if (obj2 != null)
						{
							//translationResult=(String[][])obj2;
							//adapter.updata(translationResult);
							tasks = (ArrayList<TranslationTask>)obj2;
							adapter.updata(tasks);
							itemDecoration.setTasks(tasks);
							outputRecyclerView.invalidateItemDecorations();
							//System.out.println("MainActivity:resultStrs:"+translationResult.toString()+"  length:"+translationResult.length);
							//outputText.setText(translationResult[0][0]);
							//if (!translateButton.isEnabled())
							{//当处于暂停状态时，恢复
								//translateButton.setEnabled(true);
							}
							translateProgress.setVisibility(View.INVISIBLE);
							translateButton.setVisibility(View.VISIBLE);
							//translatingProgressDialog.dismiss();
						}
						//ArrayList<TranslationTask> task
						//outputText.setText(helper.tasks.get(0).resultString);
						break;
					case 0x101:
						ApplicationUtil.print(MainActivity.this,msg.obj.toString(),msg.arg1==1);
						break;
				}
			}
		};
	}
	
	public Handler getHandler(){
		return handler;
	}
	
	private void initConsts(){
		Consts.LANGUAGE_NAMES=re.getStringArray(R.array.languages);
		Consts.ENGINE_NAMES=re.getStringArray(R.array.engines);
		Consts.TTS_NAMES=re.getStringArray(R.array.tts_engines);
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
					//矫正用户语言输入
					//暂时废除
//					short curInputLanguage=StringUtil.getLanguage(inputText.getText().toString());
//					short curChoosenLanguage=getCheckedList(sourceList).get(0).getUserData();
//					if(curInputLanguage<0){
//						
//					}
//					else if(curInputLanguage==Consts.LANGUAGE_CHINESE&&curChoosenLanguage!=Consts.LANGUAGE_CHINESE&&curChoosenLanguage!=Consts.LANGUAGE_WENYANWEN){//文言文翻译不切换
//						sourceList.get(Consts.LANGUAGE_CHINESE).setIsSelected(true);
//						sourceList.get(curChoosenLanguage).setIsSelected(false);
//						targetList.get(Consts.LANGUAGE_ENGLISH).setIsSelected(true);
//						targetList.get(Consts.LANGUAGE_CHINESE).setIsSelected(false);
//						rightTargetRv.updateData();
//						rightSourceRv.updateData();
//						ApplicationUtil.print(MainActivity.this,"检测到您当前输入的语言为【中文】\n已自动为您切换");
//					}
//					else if(curInputLanguage==Consts.LANGUAGE_ENGLISH&&curChoosenLanguage!=Consts.LANGUAGE_ENGLISH){
//						sourceList.get(Consts.LANGUAGE_ENGLISH).setIsSelected(true);
//						sourceList.get(curChoosenLanguage).setIsSelected(false);
//						targetList.get(Consts.LANGUAGE_CHINESE).setIsSelected(true);
//						targetList.get(Consts.LANGUAGE_ENGLISH).setIsSelected(false);
//						rightTargetRv.updateData();
//						rightSourceRv.updateData();
//						ApplicationUtil.print(MainActivity.this,"检测到您当前输入的语言为【英语】\n已自动为您切换");
//					}
					if(!NetworkUtil.isNetworkConnected(MainActivity.this)){
						ApplicationUtil.print(MainActivity.this,"当前似乎没有网络连接呢~");
						return;
					}
					
					//根据选择翻译
					helper = new TranslationHelper(MainActivity.this.handler);
					initTasks();
					helper.setTasks(tasks);
					helper.totalTimes = tasks.size();
					helper.start();
					
					translateProgress.setMax(100);
					translateProgress.setProgress(0);
					
					translateButton.setVisibility(View.INVISIBLE);
					translateProgress.setVisibility(View.VISIBLE);
					//dialogTranslatingProgressbar.setMax(tasks.size());
					//dialogTranslatingProgressbar.setProgress(0);
					//showTranslatingDialog();
				}
			});
			
			translateProgress=findViewById(R.id.widget_main_translate_progress);
			translateProgress.setVisibility(View.GONE);
	}
	
	private void createSlideView(){
//		View leftSlideView=LayoutInflater.from(this).inflate(R.layout.main_slide_right,null);
//		leftSlideView.setLayoutParams(new ViewGroup.LayoutParams(SmartSwipe.dp2px(280,this), ViewGroup.LayoutParams.MATCH_PARENT));
//		//SmartSwipeWrapper leftMenuWrapper = SmartSwipe.wrap(leftSlideView).addConsumer(new StretchConsumer()).enableVertical().getWrapper();
//		DrawerConsumer leftDrawerConsumer=new DrawerConsumer().setDrawerView(SwipeConsumer.DIRECTION_LEFT,leftSlideView);
//		leftDrawerConsumer.setEdgeSize(SmartSwipe.dp2px(40,this));
//		SmartSwipe.wrap(this).addConsumer(leftDrawerConsumer);
		
		View rightSlideView=LayoutInflater.from(this).inflate(R.layout.main_slide_right,null);
		SmartSwipe.wrap(rightSlideView).addConsumer(new StretchConsumer()).enableVertical();
		
		RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(SmartSwipe.dp2px(180,this), ViewGroup.LayoutParams.MATCH_PARENT);
		//params.setMargins(4,getStatusBarHeight()+100,4,4);
		rightSlideView.setLayoutParams(params);
		
		TextView tv=rightSlideView.findViewById(R.id.main_slide_right_source_tv);
		RelativeLayout.LayoutParams tvParam=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		tvParam.setMargins(0,getStatusBarHeight(),0,4);
		tv.setLayoutParams(tvParam);
		
		rightSourceRv=rightSlideView.findViewById(R.id.main_slide_right_source_rv);
		sourceList=new ArrayList<LanguageBean>();
		for(short i=0;i<Consts.LANGUAGE_NAMES.length;i++){
			LanguageBean bean=new LanguageBean();
			bean.setIsSelected(false);
			bean.setCheckKind(Consts.CHECK_SINGLE);
			bean.setUserData(i);
			bean.setText(LANGUAGE_NAMES[i]);
			sourceList.add(bean);
		}
		
		
		rightTargetRv=rightSlideView.findViewById(R.id.main_slide_right_target_rv);
		targetList=new ArrayList<LanguageBean>();
		for(short i=0;i<Consts.LANGUAGE_NAMES.length;i++){
			LanguageBean bean=new LanguageBean();
			bean.setIsSelected(false);
			bean.setCheckKind(Consts.CHECK_MULTI);
			bean.setUserData(i);
			bean.setText(LANGUAGE_NAMES[i]);
			targetList.add(bean);
		}
		
		
		LanguageRecyclerView rightEngineRv=rightSlideView.findViewById(R.id.main_slide_right_engine_rv);
		engineList=new ArrayList<LanguageBean>();
		for(short i=0;i<Consts.ENGINE_NAMES.length;i++){
			LanguageBean bean=new LanguageBean();
			bean.setIsSelected(false);
			bean.setUserData(i);
			bean.setCheckKind(Consts.CHECK_MULTI);
			bean.setText(ENGINE_NAMES[i]);
			engineList.add(bean);
		}
		
		
		LanguageRecyclerView rightTTSRv=rightSlideView.findViewById(R.id.main_slide_right_tts_rv);
		ttsList=new ArrayList<LanguageBean>();
		for(short i=0;i<Consts.TTS_NAMES.length;i++){
			LanguageBean bean=new LanguageBean();
			bean.setIsSelected(false);
			bean.setUserData(i);
			bean.setCheckKind(Consts.CHECK_SINGLE);
			bean.setText(TTS_NAMES[i]);
			ttsList.add(bean);
		}
		
		//默认勾选
		sourceList.get(Consts.LANGUAGE_CHINESE).setIsSelected(true);
		targetList.get(Consts.LANGUAGE_ENGLISH).setIsSelected(true);
		engineList.get(Consts.ENGINE_YOUDAO_NORMAL).setIsSelected(true);
		engineList.get(Consts.ENGINE_BAIDU_NORMAL).setIsSelected(true);
		ttsList.get(Consts.TTS_BAIDU).setIsSelected(true);
		
		rightSourceRv.initData(sourceList);
		rightTargetRv.initData(targetList);
		rightEngineRv.initData(engineList);
		rightTTSRv.initData(ttsList);
		
		//SmartSwipeWrapper rightMenuWrapper = SmartSwipe.wrap(rightSlideView).addConsumer(new StretchConsumer()).enableVertical().getWrapper();
		rightDrawerConsumer=new DrawerConsumer().setDrawerView(SwipeConsumer.DIRECTION_RIGHT,rightSlideView);
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
			helper.finishTasks.clear();
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
		int checkColor=Color.parseColor("#2196f3");
		int checkSize=32;
		IC_SINGLE_CHECK_CHECKED=getIcon(R.drawable.ic_single_check_checked,checkSize,checkSize,checkColor);
		IC_SINGLE_CHECK=getIcon(R.drawable.ic_single_check,checkSize,checkSize,checkColor);
		IC_MULTI_CHECK=getIcon(R.drawable.ic_multi_check,checkSize,checkSize,checkColor);
		IC_MULTI_CHECK_CHECKED=getIcon(R.drawable.ic_multi_check_checked,checkSize,checkSize,checkColor);
		IC_MENU_RIGHT_ARROW=getIcon(R.drawable.ic_menu_right_arrow,Color.WHITE);
	}
	
	private Drawable getIcon(int id,int color){
		Bitmap bitmap=BitmapUtil.getBitmapFromResources(re,id);
		BitmapDrawable b=new BitmapDrawable(bitmap);
		b.setColorFilter(color,PorterDuff.Mode.SRC_IN);
		return (Drawable)b;
	};
	
	private Drawable getIcon(int id,int targetWidth,int targetHeight,int color){
		Bitmap bitmap=BitmapUtil.getBigBitmapFromResources(re,id,targetWidth,targetHeight);
		BitmapDrawable b=new BitmapDrawable(bitmap);
		b.setColorFilter(color,PorterDuff.Mode.SRC_IN);
		return (Drawable)b;
	}
	
	private int getStatusBarHeight() {
		int resourceId = re.getIdentifier("status_bar_height", "dimen","android");
		int height = re.getDimensionPixelSize(resourceId);
		//print("statuebarh:"+height);
		return height;
	}
	
	public short getCheckedTTSEngine(){
		LanguageBean curBean=getCheckedList(ttsList).get(0);
		return curBean.getUserData();
	}
	
	private void createDialogs(){
		/*View view=LayoutInflater.from(this).inflate(R.layout.dialog_translating,null);
		dialogTranslatingContentTV=view.findViewById(R.id.dialog_translating_content_tv);
		dialogTranslatingProgressbar=view.findViewById(R.id.dialog_translating_progressbar);
		*/
		/*translatingProgressDialog=new AlertDialog.Builder(this)
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
			.create();*/
			
		if(ApplicationUtil.isFirstOpen(this)){
			tipDialog=new AlertDialog.Builder(this)
				.setTitle("Welcome")
				.setMessage(ApplicationUtil.getTextFromAssets(this,"introduction.txt"))
				.setPositiveButton("我知道啦", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface p1, int p2)
					{
						// TODO: Implement this method
						ApplicationUtil.print(MainActivity.this,"愉快玩耍吧！");
					}
				})
				.setNegativeButton("找作者", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface p1, int p2)
					{
						// TODO: Implement this method
						ApplicationUtil.copyToClipboard(MainActivity.this,"https://pic-cdn.sukiu.net/2020/01/28/mm_reward_qrcode_1572702328060_comps.md.jpg");
						ApplicationUtil.print(MainActivity.this,"网址已保存在剪贴板里喽！");
					}
				})
				.create();
			tipDialog.show();
		}
	}
	
	public void showUpdateDialog(){
			if(UpdateUtil.isUpdate){
				AlertDialog updateDialog=new AlertDialog.Builder(this)
					.setTitle("有新版！")
					.setMessage(UpdateUtil.getUpdateLog())
					.setPositiveButton("立即更新", new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface p1, int p2)
						{
							// TODO: Implement this method
							UpdateUtil.startUpdateByBrowse(MainActivity.this,UpdateUtil.getApkUrl());
							ApplicationUtil.print(MainActivity.this,"正在为您跳转……");
						}
					})
					.setNegativeButton("稍后再说", new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface p1, int p2)
						{
							// TODO: Implement this method
							ApplicationUtil.copyToClipboard(MainActivity.this,UpdateUtil.getApkUrl());
							ApplicationUtil.print(MainActivity.this,"已为您复制Apk链接，您可以稍后自行下载！");
						}
					})
					.setCancelable(false)
					.create();
				updateDialog.show();
			}else{
				AlertDialog updateDialog=new AlertDialog.Builder(this)
					.setTitle("公告！")
					.setMessage(UpdateUtil.getUpdateLog())
					.setPositiveButton("我知道了", new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface p1, int p2)
						{
							// TODO: Implement this method
						}
					})
					.setCancelable(false)
					.create();
				updateDialog.show();
			}
			//不执行后面的
	}
	
	
	public void showTranslatingDialog(){
		if(translatingProgressDialog!=null){
			translatingProgressDialog.show();
		}
	}
	
	public boolean isFree(){//判断本软件是否处于空闲状态
		if(translatingProgressDialog!=null&&translatingProgressDialog.isShowing()){
			return false;
		}
		if(tipDialog!=null&&tipDialog.isShowing()){
			return false;
		}
		if(isFinishing()||isDestroyed()||isBackground){
			return false;
		}
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		// TODO: Implement this metho
		if(keyCode==KeyEvent.KEYCODE_BACK&&event.getAction()==KeyEvent.ACTION_DOWN){
			if(!rightDrawerConsumer.isClosed()){//如果打开了右侧侧滑，就先关闭
				rightDrawerConsumer.smoothClose();
				return true;
			}
			curBackTime = System.currentTimeMillis();
			if (curBackTime - firstBackTime > 2000) {
				ApplicationUtil.print(this,"这位童鞋，再按一次退出程序哦(´-ω-`)");
				firstBackTime = curBackTime;
				return true;
			} else{
				finish();
				return true;
			}
		}
		return super.onKeyDown(keyCode,event);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.menu_main,menu);
		MenuItem menuItem=menu.getItem(0);
		menuItem.setIcon(IC_MENU_RIGHT_ARROW);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_main_right_arrow:
				if(rightDrawerConsumer.isClosed()){
                	rightDrawerConsumer.smoothRightOpen();
				}else{
					rightDrawerConsumer.smoothClose();
				}
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
	
	@Override
	protected void onPause(){
		super.onPause();
		isBackground=true;
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		isBackground=false;
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		isBackground=true;
		TTSUtil.destroyTTS();
		FunnyApplication.getProxy(this).shutdown();
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
