package com.funny.translation;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.billy.android.swipe.SmartSwipe;
import com.billy.android.swipe.SmartSwipeWrapper;
import com.billy.android.swipe.SwipeConsumer;
import com.billy.android.swipe.consumer.DrawerConsumer;
import com.billy.android.swipe.consumer.SlidingConsumer;
import com.billy.android.swipe.consumer.StretchConsumer;
import com.billy.android.swipe.listener.SimpleSwipeListener;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.funny.translation.bean.Consts;
import com.funny.translation.bean.LanguageBean;
import com.funny.translation.db.DBEnglishWords;
import com.funny.translation.db.DBEnglishWordsUtils;
import com.funny.translation.db.DBJSUtils;
import com.funny.translation.js.JS;
import com.funny.translation.js.JSBean;
import com.funny.translation.js.JSEngine;
import com.funny.translation.js.JSException;
import com.funny.translation.js.JSManager;
import com.funny.translation.js.JSUtils;
import com.funny.translation.js.TranslationCustom;
import com.funny.translation.thread.UpdateThread;
import com.funny.translation.translation.BasicTranslationTask;
import com.funny.translation.translation.TranslationBV2AV;
import com.funny.translation.translation.TranslationBaiduNormal;
import com.funny.translation.translation.TranslationBiggerText;
import com.funny.translation.translation.TranslationEachText;
import com.funny.translation.translation.TranslationGoogleNormal;
import com.funny.translation.translation.TranslationHelper;
import com.funny.translation.translation.TranslationJinshanEasy;
import com.funny.translation.translation.TranslationYouDaoNormal;
import com.funny.translation.utils.ApplicationUtil;
import com.funny.translation.utils.AutoCompleteUtil;
import com.funny.translation.utils.BitmapUtil;
import com.funny.translation.utils.ClipBoardUtil;
import com.funny.translation.utils.DataUtil;
import com.funny.translation.utils.NetworkUtil;
import com.funny.translation.utils.SharedPreferenceUtil;
import com.funny.translation.utils.StringUtil;
import com.funny.translation.utils.TTSUtil;
import com.funny.translation.utils.TimeUtil;
import com.funny.translation.utils.UpdateUtil;
import com.funny.translation.widget.DrawerAdapter;
import com.funny.translation.widget.EditTextField;
import com.funny.translation.widget.LanguageAdapter;
import com.funny.translation.widget.LanguageRecyclerView;
import com.funny.translation.widget.NewResultAdapter;
import com.funny.translation.widget.ResultItemDecoration;
import com.funny.translation.widget.WordCompleteAdapter;
import com.github.lzyzsd.circleprogress.CircleProgress;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;

import static com.funny.translation.bean.Consts.ACTIVITY_JS_MANAGE;
import static com.funny.translation.bean.Consts.ACTIVITY_MAIN;
import static com.funny.translation.bean.Consts.ACTIVITY_SETTING;
import static com.funny.translation.bean.Consts.BAIDU_APP_ID;
import static com.funny.translation.bean.Consts.BAIDU_SECURITY_KEY;
import static com.funny.translation.bean.Consts.BAIDU_SLEEP_TIME;
import static com.funny.translation.bean.Consts.DEFAULT_BAIDU_APP_ID;
import static com.funny.translation.bean.Consts.DEFAULT_BAIDU_SECURITY_KEY;
import static com.funny.translation.bean.Consts.DEFAULT_BAIDU_SLEEP_TIME;
import static com.funny.translation.bean.Consts.ENGINE_BAIDU_NORMAL;
import static com.funny.translation.bean.Consts.ENGINE_BIGGER_TEXT;
import static com.funny.translation.bean.Consts.ENGINE_BV_TO_AV;
import static com.funny.translation.bean.Consts.ENGINE_EACH_TEXT;
import static com.funny.translation.bean.Consts.ENGINE_GOOGLE;
import static com.funny.translation.bean.Consts.ENGINE_JINSHAN;
import static com.funny.translation.bean.Consts.ENGINE_JS;
import static com.funny.translation.bean.Consts.ENGINE_NAMES;
import static com.funny.translation.bean.Consts.ENGINE_YOUDAO_NORMAL;
import static com.funny.translation.bean.Consts.IC_MENU_RIGHT_ARROW;
import static com.funny.translation.bean.Consts.IC_MULTI_CHECK;
import static com.funny.translation.bean.Consts.IC_MULTI_CHECK_CHECKED;
import static com.funny.translation.bean.Consts.IC_SINGLE_CHECK;
import static com.funny.translation.bean.Consts.IC_SINGLE_CHECK_CHECKED;
import static com.funny.translation.bean.Consts.LANGUAGES;
import static com.funny.translation.bean.Consts.LANGUAGE_AUTO;
import static com.funny.translation.bean.Consts.LANGUAGE_NAMES;
import static com.funny.translation.bean.Consts.MESSAGE_FINISH_ALL_TASKS;
import static com.funny.translation.bean.Consts.MESSAGE_FINISH_CURRENT_TASK;
import static com.funny.translation.bean.Consts.MODE_NAMES;
import static com.funny.translation.bean.Consts.MODE_NORMAL;
import static com.funny.translation.bean.Consts.TTS_BAIDU;
import static com.funny.translation.bean.Consts.TTS_NAMES;

public class MainActivity extends BaseActivity 
{
	Resources re;
	String currentWord="";//自动补全 ：当前正在输入的单词
	
	Toolbar toolbar;
	EditTextField inputText;
	RecyclerView outputRecyclerView;
	RecyclerView.LayoutManager layoutManager;
	ResultItemDecoration itemDecoration;
	Button translateButton;
	ImageButton exchangeButton;
	CircleProgress translateProgress;
	
	LanguageRecyclerView rightSourceRv,rightTargetRv,rightEngineRv,rightTTSRv,rightModeRv,rightJSRv;
	TextView copyrightTextView;
	
	ArrayList<LanguageBean> sourceList,targetList,engineList,ttsList,modeList,jsList;

	DrawerConsumer rightDrawerConsumer;//右侧侧滑
	SlidingConsumer leftSlidingConsumer;
	SimpleSwipeListener swipeListener;

	ArrayList<BasicTranslationTask> tasks;
	ArrayList<BasicTranslationTask> finishTasks;

	NewResultAdapter resultAdapter;
	WordCompleteAdapter wordCompleteAdapter;
	TranslationHelper helper;
	
	Handler handler;
	DBEnglishWordsUtils dbEnglishWordsUtils;

	ArrayList<JS> allEnabledJS;
	DBJSUtils dbJSUtils;

	long curBackTime=0,firstBackTime=0;

	AlertDialog tipDialog=null;

	boolean isBackground=false;//Activity是否处于后台

	String TAG="MainActivity";
	private final static int MESSAGE_FINISH_LOAD_JS = 0x201;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
//		Date date = new Date(System.currentTimeMillis());
//		Debug.startMethodTracing(date.toString().replaceAll(" ", "_"));
        super.onCreate(savedInstanceState);
		
		setTheme(R.style.AppTheme_NoActionBar);
		re=getResources();
		setContentView(R.layout.main);
		
		new Thread(() -> {
			initConsts();
			initBitmaps();
			initEnglishWords();
			// TODO: Implement this method
		}).start();
		initMainView();
		createRightSlideView();
		initJSEngine();
		createLeftSlideView();
		createWordCompletePopup();
		initSwipeListener();
		initPreferenceDataRightRv();
		initPreferenceDataDiyBaidu();
		createDialogs();
		initHandler();
		initIntentData();//用户选择后选择用该软件翻译的

		new UpdateThread(this).start();

		inputText.requestFocus();//自动获取焦点

//		Debug.stopMethodTracing();
    }

    private void initJSEngine(){
    	dbJSUtils = DBJSUtils.getInstance();
    	allEnabledJS = dbJSUtils.queryAllEnabledJS();

    	for(int i = 0;i<allEnabledJS.size();i++){
    		JS js = allEnabledJS.get(i);
			try {
				JSEngine jsEngine = new JSEngine();
				jsEngine.loadJS(js);
				JSManager.addJSEngine(jsEngine);
			} catch (Exception e) {
				e.printStackTrace();
				allEnabledJS.remove(js);
				i--;
				ApplicationUtil.print(this,"插件【"+js.fileName+"】加载出错！原因是："+e.getMessage());
			}

		}
		jsList = JSUtils.coverJSToLanguageBean(allEnabledJS);

    	//while(rightJSRv == null) { Log.i(TAG, "rightJSRv is null"); }
		if(rightJSRv!=null) {
			int[] mapping = new int[jsList.size()];
			DataUtil.setDefaultMapping(mapping);
			rightJSRv.initData(jsList, mapping);
		}
	}

	private void initEnglishWords() {
    	dbEnglishWordsUtils = DBEnglishWordsUtils.getInstance();
	}

	private void initIntentData(){
    	Intent intent = getIntent();
    	if (intent.hasExtra("checked_text")) {//选择的
			String checkedText = intent.getStringExtra("checked_text");
			startTranslate(checkedText);
		}else if (intent.hasExtra("shared_text")){//分享的
    		String sharedText = intent.getStringExtra("shared_text");
    		startTranslate(sharedText);
		}
	}

	private void initHandler()
	{
		handler = new Handler(Looper.getMainLooper()){
			@Override
			public void handleMessage(Message msg)
			{
				switch (msg.what)
				{
					case MESSAGE_FINISH_CURRENT_TASK:
						Object obj=msg.obj;
						if (obj != null) {
							if(!(outputRecyclerView.getAdapter() instanceof NewResultAdapter))outputRecyclerView.setAdapter(resultAdapter);
							BasicTranslationTask currentFinishTask = (BasicTranslationTask)obj;
							resultAdapter.addData(currentFinishTask);

							translateProgress.setProgress(helper.getProcess());
						}
						break;
					case MESSAGE_FINISH_ALL_TASKS:
						Object obj2=msg.obj;
						if (obj2 != null) {
//							BasicTranslationTask currentFinishTask = (BasicTranslationTask)obj2;
//							tasks.add(currentFinishTask);
//							resultAdapter.addData(currentFinishTask);
							//resultAdapter.insert(tasks);
							//itemDecoration.setTasks(tasks);
							//outputRecyclerView.invalidateItemDecorations();
							translateProgress.setVisibility(View.INVISIBLE);
							translateButton.setVisibility(View.VISIBLE);
						}
						break;
					case 0x101:
						ApplicationUtil.print(MainActivity.this,msg.obj.toString(),msg.arg1==1);
						break;
					//case MESSAGE_FINISH_LOAD_JS:
//						if(rightJSRv!=null){
//							rightJSRv.initData();
//						}
//						break;

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
		MODE_NAMES=re.getStringArray(R.array.modes);
	}

	private void initMainView()
	{
		toolbar = findViewById(R.id.widget_main_toolbar);
		setSupportActionBar(toolbar);

		inputText = findViewById(R.id.widget_main_inputtext);
		inputText.addTextChangedListener(new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
				if(charSequence!=null&&charSequence.length()>0){
					if(!translateButton.isEnabled())translateButton.setEnabled(true);
//					if(before>0&&count==0){
//						char last=charSequence.charAt(start-before);
//						if('A'>last||last>'z') return;
//					}
//					if(count>1)return;
//					String text = charSequence.toString();
//					currentWord= AutoCompleteUtil.getCurrentText(text,start-before);

//
//					wordCompletePopup.showPopupWindow(translateButton);
//					TimeUtil.end();
//					DBEnglishWords.Word word = queryResults.get(0);
					//Log.i(TAG,""+queryResults.size());
				}else{
					translateButton.setEnabled(false);
				}
			}

			@Override
			public void afterTextChanged(Editable editable) {
				int place = inputText.getSelectionStart();
				//Log.i(TAG,"光标位置："+place);
				if(place == inputText.getSelectionEnd()){
					currentWord = AutoCompleteUtil.getCurrentText(editable.toString(),place>0?place-1:0);
					if(currentWord.equals(""))return;;
					Log.i(TAG,"current is "+currentWord);
					TimeUtil.start();
					ArrayList<DBEnglishWords.Word> queryResults = DBEnglishWordsUtils.getInstance().queryWords(currentWord);
					if(outputRecyclerView.getAdapter() instanceof NewResultAdapter) {
						if (queryResults.size() > 0) {
							wordCompleteAdapter.setWords(queryResults);
							outputRecyclerView.setAdapter(wordCompleteAdapter);
						}
					}else{
						wordCompleteAdapter.update(queryResults);
					}
					TimeUtil.end();
				}
			}
		});

		inputText.setClickable(true);
		inputText.setFocusable(true);

		outputRecyclerView = findViewById(R.id.widget_main_recycler_view);

		resultAdapter = new NewResultAdapter(R.layout.view_result_content,finishTasks);
		View footerView = LayoutInflater.from(this).inflate(R.layout.view_result_space,null);
		resultAdapter.addFooterView(footerView);
		resultAdapter.addChildClickViewIds(R.id.view_result_content_copy_button,R.id.view_result_content_speak_button);
		resultAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
			@Override
			public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
				NewResultAdapter resultAdapter = (NewResultAdapter)adapter;
				BasicTranslationTask task = resultAdapter.getItem(position);
				switch (view.getId()){
					case R.id.view_result_content_copy_button:
						String text = task.getResult().getBasicResult();
						ApplicationUtil.copyToClipboard(MainActivity.this, text);
						ApplicationUtil.print(MainActivity.this, "已复制翻译结果[" + (text.length() < 8 ? text : (text.substring(0, 5)) + "...") + "]到剪贴板！");
						break;

					case R.id.view_result_content_speak_button:
						short engineKind = task.engineKind;
						if (engineKind == Consts.ENGINE_BV_TO_AV || engineKind == Consts.ENGINE_BIGGER_TEXT || engineKind == Consts.ENGINE_EACH_TEXT) {
							ApplicationUtil.print(MainActivity.this, "当前引擎的翻译结果不支持朗读哦~");
							return;
						}
						short TTSEngine = getCheckedTTSEngine();
						short targetLanguage = task.targetLanguage;
						if (targetLanguage == Consts.LANGUAGE_WENYANWEN) {
							targetLanguage = Consts.LANGUAGE_CHINESE;
						}
						TTSUtil.speak(MainActivity.this, task.getResult().getBasicResult(), targetLanguage, TTSEngine);
						break;
				}
			}
		});

		itemDecoration = new ResultItemDecoration(this, tasks);
		layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
		outputRecyclerView.setAdapter(resultAdapter);
		outputRecyclerView.setLayoutManager(layoutManager);
		outputRecyclerView.addItemDecoration(itemDecoration);

		wordCompleteAdapter=new WordCompleteAdapter(this,null);
		wordCompleteAdapter.setListener(new LanguageAdapter.OnClickItemListener() {
			@Override
			public void onClick(int position) {
//				if(StringUtil.isValidContent(currentWord)){
//					DBEnglishWords.Word word=wordCompleteAdapter.getWords().get(position);
//					String input = inputText.getText().toString();
//					input=input.replace(currentWord,word.getWord());
//					inputText.setText(input);
//				}
			}
		});
		//wordCompleteDecoration=new WordCompleteDecoration();

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
					startTranslate(inputText.getText().toString());
				}
			});
		View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				if (StringUtil.isValidContent(inputText.getText().toString())){
					return true;
				}
				String content = ClipBoardUtil.get(MainActivity.this);
				inputText.setText(content);
				startTranslate(content);
				//ApplicationUtil.print(MainActivity.this,"已翻译剪切板内容！");
				return false;
			}
		};

		inputText.setOnLongClickListener(onLongClickListener);
		translateProgress=findViewById(R.id.widget_main_translate_progress);
		translateProgress.setVisibility(View.GONE);
		translateProgress.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if(helper!=null&&helper.isTranslating()){
					helper.flag = 0;
					ApplicationUtil.print(MainActivity.this,"当前任务已终止！");
					translateProgress.setVisibility(View.INVISIBLE);
					translateButton.setVisibility(View.VISIBLE);
				}
			}
		});
	}

	private void startTranslate(String content) {
//    	String[] arr = null;
//    	System.out.println(arr.length);
    	if (!StringUtil.isValidContent(content)){
			return;
		}
    	if (getCheckedList(targetList).size()==0){
    		ApplicationUtil.print(this,"您必须选择目标语言后再开始翻译！");
    		return;
		}
    	if (getCheckedList(engineList).size()==0&&getCheckedList(jsList).size()==0){
    		ApplicationUtil.print(this,"您必须选择翻译引擎再翻译！");
    		return;
		}

		content = content.trim();
		if(!NetworkUtil.isNetworkConnected(MainActivity.this)){
			ArrayList<LanguageBean> engines = getCheckedList(engineList);
			boolean onlyHasOfflineEngine = true;
			for (LanguageBean bean : engines){
				short engine = bean.getUserData();
				if (engine==ENGINE_BAIDU_NORMAL||engine==ENGINE_GOOGLE||engine==ENGINE_JINSHAN||engine==ENGINE_YOUDAO_NORMAL){
					onlyHasOfflineEngine = false;
				}
			}
			ArrayList<LanguageBean> checkedJS = getCheckedList(jsList);
			for(LanguageBean bean : checkedJS){
				JSBean jsBean = (JSBean)bean;
				boolean isOffline = JSManager.getJSEngineById(jsBean.getId()).js.isOffline;
				if(!isOffline)onlyHasOfflineEngine = false;
				break;
			}
			if(!onlyHasOfflineEngine) {
				ApplicationUtil.print(MainActivity.this, "当前似乎没有网络连接呢~");
				return;
			}
		}
		if(helper!=null&&helper.isTranslating()){
			ApplicationUtil.print(MainActivity.this,"当前翻译正在进行中，请耐心等待~");
			return;
		}
		if (inputText.getText().length()==0){//空内容
			inputText.setText(content);
		}
		//根据选择翻译
		helper = new TranslationHelper(MainActivity.this.handler);
		short mode = getCheckedList(modeList).get(0).getUserData();
		helper.setMode(mode);
		//Log.i(TAG,"正在使用的翻译模式是："+MODE_NAMES[mode]);
		initTasks(content);
		//修改为outputRV
		if(outputRecyclerView.getAdapter() instanceof WordCompleteAdapter){
			outputRecyclerView.setAdapter(resultAdapter);
//			outputRecyclerView.removeItemDecoration(wordCompleteDecoration);
//			outputRecyclerView.addItemDecoration(itemDecoration);
		}
		//resultAdapter.setList(tasks);
		helper.setTasks(tasks);
		helper.totalTimes = tasks.size();
		helper.start();

		translateProgress.setMax(100);
		translateProgress.setProgress(0);

		translateButton.setVisibility(View.INVISIBLE);
		translateProgress.setVisibility(View.VISIBLE);


	}

	private void createLeftSlideView(){
//    	getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//    	getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_rotate_narrow);

    	final View leftSlideView=LayoutInflater.from(this).inflate(R.layout.main_slide_left,null);
    	SmartSwipe.wrap(leftSlideView).addConsumer(new SlidingConsumer().enableVertical());

		RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(SmartSwipe.dp2px((int) re.getDimension(R.dimen.drawer_width),this), ViewGroup.LayoutParams.MATCH_PARENT);
		//params.setMargins(4,getStatusBarHeight()+100,4,4);
		leftSlideView.setLayoutParams(params);

		RecyclerView rv = leftSlideView.findViewById(R.id.main_slide_left_rv);
		DrawerAdapter da = new DrawerAdapter(this);
		da.setOnItemClickListener(new DrawerAdapter.OnItemClickListener() {
			@Override
			public void itemClick(DrawerAdapter.DrawerItemNormal drawerItemNormal) {
				switch (drawerItemNormal.titleRes){
					case R.string.setting:
						leftSlidingConsumer.close();
						Intent intent = new Intent();
						moveToActivityForResult(SettingActivity.class,intent,ACTIVITY_MAIN);
						break;
					case R.string.feedback:
						leftSlidingConsumer.close();
						moveToActivity(FeedbackActivity.class);
						break;
					case R.string.other_apps:
						leftSlidingConsumer.close();
						moveToActivity(OtherApplicationsActivity.class);
						break;
					case R.string.js:
						leftSlidingConsumer.close();
						moveToActivityForResult(JSManageActivity.class,new Intent(),ACTIVITY_MAIN);
						break;
				}
			}
		});
		rv.setLayoutManager(new LinearLayoutManager(this));
		rv.setAdapter(da);

		leftSlidingConsumer=new SlidingConsumer();
		leftSlidingConsumer.setDrawerView(SwipeConsumer.DIRECTION_LEFT,leftSlideView);
		leftSlidingConsumer.setEdgeSize(SmartSwipe.dp2px(40,this));
		leftSlidingConsumer.setShadowColor(Color.parseColor("#9e9e9e"));
		leftSlidingConsumer.setShadowSize(SmartSwipe.dp2px(8,this));
		SmartSwipe.wrap(this).addConsumer(leftSlidingConsumer);
	}

	private void createRightSlideView(){
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

		rightEngineRv=rightSlideView.findViewById(R.id.main_slide_right_engine_rv);
		engineList=new ArrayList<LanguageBean>();
		for(short i=0;i<Consts.ENGINE_NAMES.length;i++){
			LanguageBean bean=new LanguageBean();
			bean.setIsSelected(false);
			bean.setUserData(i);
			bean.setCheckKind(Consts.CHECK_MULTI);
			bean.setText(ENGINE_NAMES[i]);
			engineList.add(bean);
		}

		rightTTSRv=rightSlideView.findViewById(R.id.main_slide_right_tts_rv);
		ttsList=new ArrayList<LanguageBean>();
		for(short i=0;i<Consts.TTS_NAMES.length;i++){
			LanguageBean bean=new LanguageBean();
			bean.setIsSelected(false);
			bean.setUserData(i);
			bean.setCheckKind(Consts.CHECK_SINGLE);
			bean.setText(TTS_NAMES[i]);
			ttsList.add(bean);
		}

		rightModeRv=rightSlideView.findViewById(R.id.main_slide_right_mode_rv);
		modeList=new ArrayList<LanguageBean>();
		for(short i = 0; i< MODE_NAMES.length; i++){
			LanguageBean bean=new LanguageBean();
			bean.setIsSelected(false);
			bean.setUserData(i);
			bean.setCheckKind(Consts.CHECK_SINGLE);
			bean.setText(MODE_NAMES[i]);
			modeList.add(bean);
		}

		rightJSRv=rightSlideView.findViewById(R.id.main_slide_right_js_rv);
		rightJSRv.initData(new ArrayList<LanguageBean>(),new int[]{});

		//SmartSwipeWrapper rightMenuWrapper = SmartSwipe.wrap(rightSlideView).addConsumer(new StretchConsumer()).enableVertical().getWrapper();
		rightDrawerConsumer=new DrawerConsumer().setDrawerView(SwipeConsumer.DIRECTION_RIGHT,rightSlideView);
		rightDrawerConsumer.setEdgeSize(SmartSwipe.dp2px(40,this));
		rightDrawerConsumer.setShadowColor(Color.parseColor("#9e9e9e"));
		rightDrawerConsumer.setShadowSize(SmartSwipe.dp2px(8,this));
		SmartSwipe.wrap(this).addConsumer(rightDrawerConsumer);

		exchangeButton = findViewById(R.id.main_slide_right_exchange_button);
		exchangeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				LanguageBean checkedSourceLanguage = getCheckedList(sourceList).get(0);
				ArrayList<LanguageBean> checkedTargetLanguages = getCheckedList(targetList);
				LanguageBean checkedTargetLanguage;
				if(checkedTargetLanguages.isEmpty()){
					disSelectList(targetList);
					targetList.get(checkedSourceLanguage.getUserData()).setIsSelected(true);
					sourceList.get(checkedSourceLanguage.getUserData()).setIsSelected(false);
					sourceList.get(LANGUAGE_AUTO).setIsSelected(true);
					rightSourceRv.updateData();
					rightTargetRv.updateData();
//					targetList.get(checkedSourceLanguage)
//					return;
				}else{
					assert rightTargetRv.getAdapter() != null;
					int[] mapping = rightTargetRv.getAdapter().getMapping();
					for (int each : mapping) {
						if (targetList.get(each).isSelected()){
							checkedTargetLanguage = targetList.get(each);
							disSelectList(targetList);
							targetList.get(checkedSourceLanguage.getUserData()).setIsSelected(true);
							sourceList.get(checkedSourceLanguage.getUserData()).setIsSelected(false);
							sourceList.get(checkedTargetLanguage.getUserData()).setIsSelected(true);
							rightSourceRv.updateData();
							rightTargetRv.updateData();
							break;
						}
					}
				}
				Log.i(TAG,"click button!");
			}
		});

		String copyrightInfo="Copyright@FunnySaltyFish\n2020\nAll Rights Reserved";
		copyrightTextView=findViewById(R.id.main_slide_right_copyright);
		SpannableStringBuilder ssb=new SpannableStringBuilder();
		ssb.append(copyrightInfo);
		ClickableSpan sc=new ClickableSpan(){
			@Override
			public void onClick(View p1)
			{
				// TODO: Implement this method
				String url="https://imgconvert.csdnimg.cn/aHR0cHM6Ly9hZTAxLmFsaWNkbi5jb20va2YvSGJmZmU2OWQyNTE0ZDRhZjhhZDBmYzgwNjY4YzU3ZDU0US5wbmc?x-oss-process=image/format,png";
				String url2="https://i.loli.net/2020/04/05/PBtK4pv5nR6Cdme.png";
				Intent intent=new Intent();
				Uri uri;
				if(Math.random()<0.5){
					uri=Uri.parse(url);
				}else{
					uri=Uri.parse(url2);
				}
				intent.setAction(Intent.ACTION_VIEW);
				intent.setData(uri);
				MainActivity.this.startActivity(intent);
			}
		};
		int start=copyrightInfo.indexOf("FunnySaltyFish");
		int end=start+"FunnySaltyFish".length();
		ssb.setSpan(sc,start,end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.parseColor("#2196f3"));
		ssb.setSpan(foregroundColorSpan,start,end,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		//配置给TextView
		copyrightTextView.setMovementMethod(LinkMovementMethod.getInstance());
		copyrightTextView.setText(ssb);
//		exchangeButton.setLayoutParams(tvParam);
	}

	private void createWordCompletePopup(){
    	//wordCompletePopup = new WordCompletePopup(this,null);
	}

	private void disSelectList(@NonNull ArrayList<LanguageBean> list){
    	for (LanguageBean item:list){
    		item.setIsSelected(false);
		}
	}

	private void initPreferenceDataRightRv(){
    	disSelectList(sourceList);
    	disSelectList(targetList);
    	disSelectList(engineList);
    	disSelectList(ttsList);
    	disSelectList(modeList);

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		int checkedSourceLanguage =Integer.parseInt(Objects.requireNonNull(sp.getString("preference_language_source_default", "1")));
		sourceList.get(checkedSourceLanguage).setIsSelected(true);
		//Log.i(TAG,"____获取到的checkedSourceL is "+checkedSourceLanguage);

		String[] checkedTargetLanguages=sp.getStringSet("preference_language_target_default",new HashSet<String>()).toArray(new String[0]);
		if (checkedTargetLanguages.length!=0){
			for (int i = 0; i < checkedTargetLanguages.length; i++) {
				targetList.get(Integer.parseInt(checkedTargetLanguages[i])).setIsSelected(true);
				//Log.i(TAG,"____获取到的checkedTargetL 包括 "+checkedTargetLanguages[i]);
			}
		}else{
			targetList.get(2).setIsSelected(true);
		}

		String[] checkedEngines=sp.getStringSet("preference_engines_default",new HashSet<String>()).toArray(new String[0]);
		if (checkedEngines.length!=0){
			for (int i = 0; i < checkedEngines.length; i++) {
				engineList.get(Integer.parseInt(checkedEngines[i])).setIsSelected(true);
			}
		}else{
			engineList.get(ENGINE_GOOGLE).setIsSelected(true);
			engineList.get(ENGINE_YOUDAO_NORMAL).setIsSelected(true);
		}

		String pre_language_mapping_string = SharedPreferenceUtil.getInstance().getString("pre_language_mapping_string","");
		int[] languageMapping;
		if (pre_language_mapping_string.equals("")){
			languageMapping=new int[LANGUAGES.length];
			DataUtil.setDefaultMapping(languageMapping);
		}
		else{
			languageMapping = DataUtil.coverStringToIntArray(pre_language_mapping_string);
		}
		rightSourceRv.initData(sourceList,languageMapping);
		rightTargetRv.initData(targetList,languageMapping);

		int[] engineMapping=new int[ENGINE_NAMES.length];
		DataUtil.setDefaultMapping(engineMapping);
		rightEngineRv.initData(engineList,engineMapping);

		int[] ttsMapping=new int[TTS_NAMES.length];
		DataUtil.setDefaultMapping(ttsMapping);
		rightTTSRv.initData(ttsList,ttsMapping);

		int[] modeMapping=new int[MODE_NAMES.length];
		DataUtil.setDefaultMapping(modeMapping);
		rightModeRv.initData(modeList,modeMapping);

//		int[] jsMapping = new int[MODE_NAMES.length];
//		DataUtil.setDefaultMapping(jsMapping);
//		rightJSRv.initData(jsList,jsMapping);


		ttsList.get(TTS_BAIDU).setIsSelected(true);
		modeList.get(MODE_NORMAL).setIsSelected(true);
	}


	private void initPreferenceDataDiyBaidu(){
		//百度翻译 自定义
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		Log.i(TAG,"是否使用自定义百度？"+sp.getBoolean("preference_baidu_is_diy_api",false));
		if (sp.getBoolean("preference_baidu_is_diy_api",false)) {
			BAIDU_APP_ID = sp.getString("preference_baidu_diy_appid",DEFAULT_BAIDU_APP_ID);
			BAIDU_SECURITY_KEY = sp.getString("preference_baidu_diy_key",DEFAULT_BAIDU_SECURITY_KEY);
			BAIDU_SLEEP_TIME = Long.parseLong(Objects.requireNonNull(sp.getString("preference_baidu_diy_sleep_time", String.valueOf(DEFAULT_BAIDU_SLEEP_TIME))));
		}else{
			BAIDU_APP_ID = DEFAULT_BAIDU_APP_ID;
			BAIDU_SECURITY_KEY = DEFAULT_BAIDU_SECURITY_KEY;
			BAIDU_SLEEP_TIME = DEFAULT_BAIDU_SLEEP_TIME;
		}
	}

	private void initSwipeListener(){
    	swipeListener=new SimpleSwipeListener(){
			@Override
			public void onSwipeOpened(SmartSwipeWrapper wrapper, SwipeConsumer consumer, int direction) {
				if(consumer.getClass() == rightDrawerConsumer.getClass()){
					//如果打开的是右边的
					if (leftSlidingConsumer.isOpened()){
						leftSlidingConsumer.smoothClose();
					}
				}
				else if (consumer.getClass()==leftSlidingConsumer.getClass()){
					if (rightDrawerConsumer.isOpened()){
						rightDrawerConsumer.smoothClose();
					}
				}
				super.onSwipeOpened(wrapper, consumer, direction);
			}

			@Override
			public void onSwipeProcess(SmartSwipeWrapper wrapper, SwipeConsumer consumer, int direction, boolean settling, float progress) {
				if(consumer.getClass() == rightDrawerConsumer.getClass()){
					//如果打开的是右边的
					if (leftSlidingConsumer.isOpened()){
						leftSlidingConsumer.close();
					}
				}
				else if (consumer.getClass()==leftSlidingConsumer.getClass()){
					if (rightDrawerConsumer.isOpened()){
						rightDrawerConsumer.smoothClose();
					}
				}
				super.onSwipeProcess(wrapper, consumer, direction, settling, progress);
			}

			@Override
			public void onSwipeClosed(SmartSwipeWrapper wrapper, SwipeConsumer consumer, int direction) {
				super.onSwipeClosed(wrapper, consumer, direction);
			}
		};
    	leftSlidingConsumer.addListener(swipeListener);
    	rightDrawerConsumer.addListener(swipeListener);
	}
	
	private void initTasks(String content){
		if(tasks==null){
			tasks=new ArrayList<BasicTranslationTask>();
		}else{
			tasks.clear();
			helper.finishTasks.clear();
		}

		if(finishTasks==null){
			finishTasks=new ArrayList<>();
		}else {
			finishTasks.clear();
		}
		resultAdapter.setList(finishTasks);

		short source=getCheckedList(sourceList).get(0).getUserData();
		int i=0;
		BasicTranslationTask task;
		for(LanguageBean target:getCheckedList(targetList)){
			for(LanguageBean engine:getCheckedList(engineList)){
				switch (engine.getUserData()){
					case ENGINE_JINSHAN:
						//task = new TranslationYouDaoEasy(helper,content,source,target.getUserData(),ENGINE_YOUDAO_EASY);
						task = new TranslationJinshanEasy(helper,content,source,target.getUserData(),ENGINE_JINSHAN);
						break;
					case ENGINE_YOUDAO_NORMAL:
						task = new TranslationYouDaoNormal(helper,content,source,target.getUserData(),ENGINE_YOUDAO_NORMAL);
						break;
					case ENGINE_BAIDU_NORMAL:
						task = new TranslationBaiduNormal(helper,content,source,target.getUserData(),ENGINE_BAIDU_NORMAL);
						break;
					case ENGINE_GOOGLE:
						task = new TranslationGoogleNormal(helper,content,source,target.getUserData(),ENGINE_GOOGLE);
						break;
					case ENGINE_BV_TO_AV:
						task = new TranslationBV2AV(helper,content,source,target.getUserData(),ENGINE_BV_TO_AV);
						break;
					case ENGINE_BIGGER_TEXT:
						task = new TranslationBiggerText(helper,content,source,target.getUserData(),ENGINE_BIGGER_TEXT);
						break;
					case ENGINE_EACH_TEXT:
						task = new TranslationEachText(helper,content,source,target.getUserData(),ENGINE_EACH_TEXT);
						break;
					default:
						throw new IllegalStateException("没有这个引擎: " + engine.getUserData());
				}
				//task.setId(i++);
				//task.setLisener(helper.defaultListener);
				tasks.add(task);
			}
			//处理插件
			for (LanguageBean bean : getCheckedList(jsList)){
				JSBean jsBean = (JSBean)bean;
				JSEngine jsEngine = JSManager.getJSEngineById(jsBean.getId());
				if(jsEngine!=null){
					TranslationCustom customTask = new TranslationCustom(helper,content,source,target.getUserData(),ENGINE_JS);
					customTask.setJSEngine(jsEngine);
					tasks.add(customTask);
				}
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
		return b;
	};
	
	@NonNull
	private Drawable getIcon(int id, int targetWidth, int targetHeight, int color){
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
//						ApplicationUtil.copyToClipboard(MainActivity.this,"https://i.loli.net/2020/04/05/PBtK4pv5nR6Cdme.png");
//						ApplicationUtil.print(MainActivity.this,"网址已保存在剪贴板里喽！");
						String url="https://imgconvert.csdnimg.cn/aHR0cHM6Ly9hZTAxLmFsaWNkbi5jb20va2YvSGJmZmU2OWQyNTE0ZDRhZjhhZDBmYzgwNjY4YzU3ZDU0US5wbmc?x-oss-process=image/format,png";
						String url2="https://i.loli.net/2020/04/05/PBtK4pv5nR6Cdme.png";
						Intent intent=new Intent();
						Uri uri;
						if(Math.random()<0.5){
							uri=Uri.parse(url);
						}else{
							uri=Uri.parse(url2);
						}
						intent.setAction(Intent.ACTION_VIEW);
						intent.setData(uri);
						MainActivity.this.startActivity(intent);
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

	public boolean isFree(){//判断本软件是否处于空闲状态
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
		// TODO: Implement this method
		if(keyCode==KeyEvent.KEYCODE_BACK&&event.getAction()==KeyEvent.ACTION_DOWN){
			if(!rightDrawerConsumer.isClosed()){//如果打开了右侧侧滑，就先关闭
				rightDrawerConsumer.smoothClose();
				return true;
			}
			if (leftSlidingConsumer.isOpened()){
				leftSlidingConsumer.smoothClose();
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
			case android.R.id.home:
				if (leftSlidingConsumer.isOpened()){
					leftSlidingConsumer.smoothClose();
					//item.setIcon(R.drawable.ic_menu);
				}else{
					leftSlidingConsumer.smoothLeftOpen();
					//item.setIcon(R.drawable.ic_menu_narrow_left);
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
		isBackground=true;
		TTSUtil.destroyTTS();
		FunnyApplication.getProxy(this).shutdown();
		dbEnglishWordsUtils.close();
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//Log.i(TAG,"onActivityResult。收到的requestCode:"+requestCode+"  resultCode is"+resultCode);
		if (requestCode==ACTIVITY_MAIN&&resultCode==ACTIVITY_SETTING){
			assert data != null;
			boolean isRvChange=data.getBooleanExtra("isRvChange",false);//targetList、sourceList内容是否改变
			boolean isDiyBaiduChange = data.getBooleanExtra("isDiyBaiduChange",false);//是否自定义百度
			//Log.i(TAG,"_____获取到传回的isRvChange额为:"+isRvChange);
			if (isRvChange) initPreferenceDataRightRv();
			if (isDiyBaiduChange)initPreferenceDataDiyBaidu();
		}
		else if(requestCode==ACTIVITY_MAIN&&resultCode==ACTIVITY_JS_MANAGE){
			assert data != null;
			boolean hasChanged = data.getBooleanExtra("hasChanged",false);
			if(hasChanged){
				JSManager.clearAllEngines();
				initJSEngine();
			}
		}
	}

}
