package com.funny.translation

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.*
import android.view.View.OnLongClickListener
import android.widget.Button
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.billy.android.swipe.SmartSwipe
import com.billy.android.swipe.SmartSwipeWrapper
import com.billy.android.swipe.SwipeConsumer
import com.billy.android.swipe.consumer.DrawerConsumer
import com.billy.android.swipe.consumer.SlidingConsumer
import com.billy.android.swipe.consumer.StretchConsumer
import com.billy.android.swipe.listener.SimpleSwipeListener
import com.chad.library.adapter.base.BaseQuickAdapter
import com.funny.translation.bean.Consts
import com.funny.translation.bean.LanguageBean
import com.funny.translation.db.DBEnglishWordsUtils
import com.funny.translation.db.DBJSUtils
import com.funny.translation.helper.showMessageDialog
import com.funny.translation.js.JSLanguageBean
import com.funny.translation.js.JSUtils
import com.funny.translation.js.JsEngine
import com.funny.translation.js.JsManager.addJSEngine
import com.funny.translation.js.JsManager.clear
import com.funny.translation.js.JsManager.getJsEngineById
import com.funny.translation.js.bean.JsBean
import com.funny.translation.js.core.JsTranslateTask
import com.funny.translation.thread.UpdateThread
import com.funny.translation.trans.CoreTranslationTask
import com.funny.translation.translation.*
import com.funny.translation.utils.*
import com.funny.translation.widget.*
import com.funny.translation.widget.DrawerAdapter.DrawerItemNormal
import com.github.lzyzsd.circleprogress.CircleProgress
import java.util.*

class MainActivity : BaseActivity() {
    var re: Resources? = null
    var currentWord = "" //自动补全 ：当前正在输入的单词
    lateinit var toolbar: Toolbar
    lateinit var inputText: EditTextField
    lateinit var outputRecyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var itemDecoration: ResultItemDecoration
    lateinit var translateButton: Button
    lateinit var exchangeButton: ImageButton
    lateinit var translateProgress: CircleProgress
    lateinit var rightSourceRv: LanguageRecyclerView
    lateinit var rightTargetRv: LanguageRecyclerView
    lateinit var rightEngineRv: LanguageRecyclerView
    lateinit var rightTTSRv: LanguageRecyclerView
    lateinit var rightModeRv: LanguageRecyclerView
    lateinit var rightJSRv: LanguageRecyclerView
    lateinit var copyrightTextView: TextView
    lateinit var sourceList: ArrayList<LanguageBean>
    lateinit var targetList: ArrayList<LanguageBean>
    lateinit var engineList: ArrayList<LanguageBean>
    lateinit var ttsList: ArrayList<LanguageBean>
    lateinit var modeList: ArrayList<LanguageBean>
    lateinit var jsList: ArrayList<LanguageBean>
    lateinit var rightDrawerConsumer: DrawerConsumer //右侧侧滑
    lateinit var leftSlidingConsumer: SlidingConsumer
    lateinit var swipeListener: SimpleSwipeListener
    val tasks: ArrayList<CoreTranslationTask> by lazy{ arrayListOf() }
    lateinit var resultAdapter: NewResultAdapter
    lateinit var wordCompleteAdapter: WordCompleteAdapter
    lateinit var helper: NewTranslationHelper
    val onTranslateListener: OnTranslateListener = object : OnTranslateListener {
        override fun finishOne(task: CoreTranslationTask, e: Exception?) {
            e?.printStackTrace()

            val msg = handler.obtainMessage()
            msg.what = Consts.MESSAGE_FINISH_CURRENT_TASK
            msg.obj = task
            //没问题
            //Log.d(TAG, "finishOne: " + helper.progress +"result:${task.result}")
            msg.arg1 = helper.progress
            handler.sendMessage(msg)
        }

        override fun finishAll() {
            val msg = handler.obtainMessage()
            msg.what = Consts.MESSAGE_FINISH_ALL_TASKS
            msg.arg1 = 1
            handler.sendMessage(msg)
        }
    }
    lateinit var handler: Handler
    lateinit var dbEnglishWordsUtils: DBEnglishWordsUtils
    lateinit var allEnabledJS: ArrayList<JsBean>
    lateinit var dbJSUtils: DBJSUtils
    var curBackTime: Long = 0
    var firstBackTime: Long = 0
    var isBackground = false //Activity是否处于后台
    val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme_NoActionBar)
        re = resources
        setContentView(R.layout.main)
        helper = NewTranslationHelper.getInstance()
        helper.setOnTranslateListener(onTranslateListener)
        Thread {
            initConsts()
            initBitmaps()
            initEnglishWords()
        }.start()
        initMainView()
        createRightSlideView()
        initJsEngine()
        createLeftSlideView()
        createWordCompletePopup()
        initSwipeListener()
        initPreferenceDataRightRv()
        initPreferenceDataDiyBaidu()
        createDialogs()
        initHandler()
        initIntentData() //用户选择后选择用该软件翻译的
        UpdateThread(this).start()
        inputText.requestFocus() //自动获取焦点

        //Debug
        //this.finish();
        //moveToActivity(CodeEditorActivity::class.java)
    }

    private fun initJsEngine() {
        dbJSUtils = DBJSUtils
        allEnabledJS = dbJSUtils.queryAllEnabledJS()
        var i = 0
        while (i < allEnabledJS.size) {
            val jsBean = allEnabledJS[i]
            try {
                val jsEngine = JsEngine(jsBean)
                addJSEngine(jsEngine)
            } catch (e: Exception) {
                e.printStackTrace()
                allEnabledJS.remove(jsBean)
                i--
                ApplicationUtil.print(this, "插件【" + jsBean.fileName + "】加载出错！原因是：" + e.message)
            }
            i++
        }
        jsList = JSUtils.coverJSToLanguageBean(allEnabledJS)

        //while(rightJSRv == null) { Log.i(TAG, "rightJSRv is null"); }
        if (rightJSRv != null) {
            val mapping = IntArray(jsList.size)
            DataUtil.setDefaultMapping(mapping)
            rightJSRv.initData(jsList, mapping)
        }
    }

    private fun initEnglishWords() {
        dbEnglishWordsUtils = DBEnglishWordsUtils.getInstance()
    }

    private fun initIntentData() {
        val intent = intent
        if (intent.hasExtra("checked_text")) { //选择的
            val checkedText = intent.getStringExtra("checked_text")?:""
            startTranslate(checkedText)
        } else if (intent.hasExtra("shared_text")) { //分享的
            val sharedText = intent.getStringExtra("shared_text")?:""
            startTranslate(sharedText)
        }
    }

    private fun initHandler() {
        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    Consts.MESSAGE_FINISH_CURRENT_TASK -> {
                        val obj = msg.obj
                        if (obj != null) {
                            if (outputRecyclerView.adapter !is NewResultAdapter) outputRecyclerView.adapter =
                                resultAdapter
                            val currentFinishTask = obj as CoreTranslationTask
                            //这里也没问题
                            //Log.d(TAG, "handleMessage: receive a data:${currentFinishTask.result.basicResult.hashCode()}")

                            resultAdapter.addData(currentFinishTask)
                            translateProgress.progress = msg.arg1
                            //接收完数据后原来的那个被改了
                        }
                    }
                    Consts.MESSAGE_FINISH_ALL_TASKS -> {
                        translateProgress.visibility = View.INVISIBLE
                        translateButton.visibility = View.VISIBLE

                        Log.d(TAG, "handleMessage: finish_all_tasks")
                    }
                    0x101 -> ApplicationUtil.print(
                        this@MainActivity,
                        msg.obj.toString(),
                        msg.arg1 == 1
                    )
                }
            }
        }
    }

    private fun initConsts() {
        Consts.LANGUAGE_NAMES = resources.getStringArray(R.array.languages)
        Consts.ENGINE_NAMES = resources.getStringArray(R.array.engines)
        Consts.TTS_NAMES = resources.getStringArray(R.array.tts_engines)
        Consts.MODE_NAMES = resources.getStringArray(R.array.modes)
    }

    private fun initMainView() {
        toolbar = findViewById(R.id.widget_main_toolbar)
        setSupportActionBar(toolbar)
        inputText = findViewById(R.id.widget_main_inputtext)
        inputText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(
                charSequence: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (charSequence.isNotEmpty()) {
                    if (!translateButton.isEnabled) translateButton.isEnabled = true
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
                } else {
                    translateButton.isEnabled = false
                }
            }

            override fun afterTextChanged(editable: Editable) {
                val place = inputText.getSelectionStart()
                //Log.i(TAG,"光标位置："+place);
                if (place == inputText.getSelectionEnd()) {
                    currentWord = AutoCompleteUtil.getCurrentText(
                        editable.toString(),
                        if (place > 0) place - 1 else 0
                    )
                    if (currentWord == "") return
                    Log.i(TAG, "current is $currentWord")
                    TimeUtil.start()
                    val queryResults = DBEnglishWordsUtils.getInstance().queryWords(currentWord)
                    if (outputRecyclerView.adapter is NewResultAdapter) {
                        if (queryResults.size > 0) {
                            wordCompleteAdapter.words = queryResults
                            outputRecyclerView.adapter = wordCompleteAdapter
                        }
                    } else {
                        wordCompleteAdapter.update(queryResults)
                    }
                    TimeUtil.end()
                }
            }
        })
        inputText.setClickable(true)
        inputText.setFocusable(true)
        outputRecyclerView = findViewById(R.id.widget_main_recycler_view)
        resultAdapter = NewResultAdapter(R.layout.view_result_content)
        val footerView = LayoutInflater.from(this).inflate(R.layout.view_result_space, null)
        resultAdapter.addFooterView(footerView)
        resultAdapter.addChildClickViewIds(
            R.id.view_result_content_copy_button,
            R.id.view_result_content_speak_button
        )
        resultAdapter.setOnItemChildClickListener { adapter: BaseQuickAdapter<*, *>, view: View, position: Int ->
            val task = (adapter as NewResultAdapter).data[position]
            when (view.id) {
                R.id.view_result_content_copy_button -> {
                    val text = task.result.basicResult.trans
                    ApplicationUtil.copyToClipboard(this@MainActivity, text)
                    ApplicationUtil.print(
                        this@MainActivity,
                        "已复制翻译结果[" + (if (text.length < 8) text else text.substring(
                            0,
                            5
                        ) + "...") + "]到剪贴板！"
                    )
                }
                R.id.view_result_content_speak_button -> {
                    val engineKind = task.engineName
                    if (engineKind == Consts.ENGINE_BV_TO_AV || engineKind == Consts.ENGINE_BIGGER_TEXT || engineKind == Consts.ENGINE_EACH_TEXT) {
                        ApplicationUtil.print(this@MainActivity, "当前引擎的翻译结果不支持朗读哦~")
                        return@setOnItemChildClickListener
                    }
                    val TTSEngine = checkedTTSEngine
                    var targetLanguage = task.targetLanguage
                    if (targetLanguage == Consts.LANGUAGE_WENYANWEN) {
                        targetLanguage = Consts.LANGUAGE_CHINESE
                    }
                    TTSUtil.speak(
                        this@MainActivity,
                        task.result.basicResult.trans,
                        targetLanguage,
                        TTSEngine
                    )
                }
            }
        }
        itemDecoration = ResultItemDecoration(this, tasks)
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        outputRecyclerView.setAdapter(resultAdapter)
        outputRecyclerView.setLayoutManager(layoutManager)
        outputRecyclerView.addItemDecoration(itemDecoration)
        wordCompleteAdapter = WordCompleteAdapter(this, null)
        wordCompleteAdapter.setListener { position: Int -> }
        //wordCompleteDecoration=new WordCompleteDecoration();
        translateButton = findViewById(R.id.widget_main_translate)
        translateButton.setClickable(true)
        translateButton.setEnabled(false)
        translateButton.setOnClickListener({ view: View? ->
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
            startTranslate(inputText.getText().toString())
        })
        val onLongClickListener = OnLongClickListener {
            if (StringUtil.isValidContent(inputText.getText().toString())) {
                return@OnLongClickListener true
            }
            val content = ClipBoardUtil.get(this@MainActivity)
            inputText.setText(content)
            startTranslate(content)
            //ApplicationUtil.print(MainActivity.this,"已翻译剪切板内容！");
            false
        }
        inputText.setOnLongClickListener(onLongClickListener)
        translateProgress = findViewById(R.id.widget_main_translate_progress)
        translateProgress.setVisibility(View.GONE)
        translateProgress.setOnClickListener(View.OnClickListener {
            if (helper != null) {
                helper.stopTasks()
                ApplicationUtil.print(this@MainActivity, "当前任务已终止！")
                translateProgress.setVisibility(View.INVISIBLE)
                translateButton.setVisibility(View.VISIBLE)
            }
        })
    }

    private fun startTranslate(content: String) {
//    	String[] arr = null;
//    	System.out.println(arr.length);
        if (!StringUtil.isValidContent(content)) {
            return
        }
        if (getCheckedList(targetList).size == 0) {
            ApplicationUtil.print(this, "您必须选择目标语言后再开始翻译！")
            return
        }
        if (getCheckedList(engineList).size == 0 && getCheckedList(jsList).size == 0) {
            ApplicationUtil.print(this, "您必须选择翻译引擎再翻译！")
            return
        }
        val processedContext = content.trim { it <= ' ' }
        if (!NetworkUtil.isNetworkConnected(this@MainActivity)) {
            val engines = getCheckedList(engineList)
            var onlyHasOfflineEngine = true
            for (bean in engines) {
                val engine = bean.userData
                if (engine == Consts.ENGINE_BAIDU_NORMAL || engine == Consts.ENGINE_GOOGLE || engine == Consts.ENGINE_JINSHAN || engine == Consts.ENGINE_YOUDAO_NORMAL) {
                    onlyHasOfflineEngine = false
                    break
                }
            }
            val checkedJS = getCheckedList(jsList)
            for (bean in checkedJS) {
                val jsLanguageBean = bean as JSLanguageBean
                val jsEngine = getJsEngineById(jsLanguageBean.id)
                if (jsEngine != null && !jsEngine.isOffline) {
                    onlyHasOfflineEngine = false
                    break
                }
                break
            }
            if (!onlyHasOfflineEngine) {
                ApplicationUtil.print(this@MainActivity, "当前似乎没有网络连接呢~")
                return
            }
        }
        if (helper != null && helper.isTranslating) {
            ApplicationUtil.print(this@MainActivity, "当前翻译正在进行中，请耐心等待~")
            return
        }
        if (inputText.text!!.isEmpty()) { //空内容
            inputText.setText(content)
        }
        //根据选择翻译
        val mode = getCheckedList(modeList)[0].userData
        helper.setMode(mode)
        //Log.i(TAG,"正在使用的翻译模式是："+MODE_NAMES[mode]);
        initTasks(content)
        //修改为outputRV
        if (outputRecyclerView.adapter is WordCompleteAdapter) {
            outputRecyclerView.adapter = resultAdapter
            //			outputRecyclerView.removeItemDecoration(wordCompleteDecoration);
//			outputRecyclerView.addItemDecoration(itemDecoration);
        }
        //resultAdapter.setList(tasks);
        helper.initTasks(tasks)
        translateProgress.max = 100
        translateProgress.progress = 0
        translateButton.visibility = View.INVISIBLE
        translateProgress.visibility = View.VISIBLE
    }

    private fun createLeftSlideView() {
//    	getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//    	getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_rotate_narrow);
        val leftSlideView = LayoutInflater.from(this).inflate(R.layout.main_slide_left, null)
        SmartSwipe.wrap(leftSlideView).addConsumer(SlidingConsumer().enableVertical())
        val params = RelativeLayout.LayoutParams(
            SmartSwipe.dp2px(
                resources.getDimension(R.dimen.drawer_width).toInt(), this
            ), ViewGroup.LayoutParams.MATCH_PARENT
        )
        //params.setMargins(4,getStatusBarHeight()+100,4,4);
        leftSlideView.layoutParams = params
        val rv: RecyclerView = leftSlideView.findViewById(R.id.main_slide_left_rv)
        val da = DrawerAdapter(this)
        da.setOnItemClickListener { drawerItemNormal: DrawerItemNormal ->
            when (drawerItemNormal.titleRes) {
                R.string.setting -> {
                    leftSlidingConsumer.close()
                    val intent = Intent()
                    moveToActivityForResult(
                        SettingActivity::class.java,
                        intent,
                        Consts.ACTIVITY_MAIN
                    )
                }
                R.string.feedback -> {
                    leftSlidingConsumer.close()
                    moveToActivity(FeedbackActivity::class.java)
                }
                R.string.other_apps -> {
                    leftSlidingConsumer.close()
                    moveToActivity(OtherApplicationsActivity::class.java)
                }
                R.string.js -> {
                    leftSlidingConsumer.close()
                    moveToActivityForResult(
                        JSManageActivity::class.java,
                        Intent(),
                        Consts.ACTIVITY_MAIN
                    )
                }
            }
        }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = da
        leftSlidingConsumer = SlidingConsumer()
        leftSlidingConsumer.setDrawerView(SwipeConsumer.DIRECTION_LEFT, leftSlideView)
        leftSlidingConsumer.edgeSize = SmartSwipe.dp2px(40, this)
        leftSlidingConsumer.setShadowColor(Color.parseColor("#9e9e9e"))
        leftSlidingConsumer.shadowSize = SmartSwipe.dp2px(8, this)
        SmartSwipe.wrap(this).addConsumer(leftSlidingConsumer)
    }

    private fun createRightSlideView() {
        val rightSlideView = LayoutInflater.from(this).inflate(R.layout.main_slide_right, null)
        SmartSwipe.wrap(rightSlideView).addConsumer(StretchConsumer()).enableVertical()
        val params = RelativeLayout.LayoutParams(
            SmartSwipe.dp2px(180, this),
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        //params.setMargins(4,getStatusBarHeight()+100,4,4);
        rightSlideView.layoutParams = params
        val tv = rightSlideView.findViewById<TextView>(R.id.main_slide_right_source_tv)
        val tvParam = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        tvParam.setMargins(0, statusBarHeight, 0, 4)
        tv.layoutParams = tvParam
        rightSourceRv = rightSlideView.findViewById(R.id.main_slide_right_source_rv)
        sourceList = ArrayList()
        for (i in Consts.LANGUAGE_NAMES.indices) {
            val bean = LanguageBean()
            bean.setIsSelected(false)
            bean.checkKind = Consts.CHECK_SINGLE
            bean.userData = i.toShort()
            bean.setText(Consts.LANGUAGE_NAMES[i])
            sourceList.add(bean)
        }
        rightTargetRv = rightSlideView.findViewById(R.id.main_slide_right_target_rv)
        targetList = ArrayList()
        for (i in Consts.LANGUAGE_NAMES.indices) {
            val bean = LanguageBean()
            bean.setIsSelected(false)
            bean.checkKind = Consts.CHECK_MULTI
            bean.userData = i.toShort()
            bean.setText(Consts.LANGUAGE_NAMES[i])
            targetList.add(bean)
        }
        rightEngineRv = rightSlideView.findViewById(R.id.main_slide_right_engine_rv)
        engineList = ArrayList()
        for (i in Consts.ENGINE_NAMES.indices) {
            val bean = LanguageBean()
            bean.setIsSelected(false)
            bean.userData = i.toShort()
            bean.checkKind = Consts.CHECK_MULTI
            bean.setText(Consts.ENGINE_NAMES[i])
            engineList.add(bean)
        }
        rightTTSRv = rightSlideView.findViewById(R.id.main_slide_right_tts_rv)
        ttsList = ArrayList()
        for (i in Consts.TTS_NAMES.indices) {
            val bean = LanguageBean()
            bean.setIsSelected(false)
            bean.userData = i.toShort()
            bean.checkKind = Consts.CHECK_SINGLE
            bean.setText(Consts.TTS_NAMES[i])
            ttsList.add(bean)
        }
        rightModeRv = rightSlideView.findViewById(R.id.main_slide_right_mode_rv)
        modeList = ArrayList()
        for (i in Consts.MODE_NAMES.indices) {
            val bean = LanguageBean()
            bean.setIsSelected(false)
            bean.userData = i.toShort()
            bean.checkKind = Consts.CHECK_SINGLE
            bean.setText(Consts.MODE_NAMES[i])
            modeList.add(bean)
        }
        rightJSRv = rightSlideView.findViewById(R.id.main_slide_right_js_rv)
        rightJSRv.initData(ArrayList(), intArrayOf())

        //SmartSwipeWrapper rightMenuWrapper = SmartSwipe.wrap(rightSlideView).addConsumer(new StretchConsumer()).enableVertical().getWrapper();
        rightDrawerConsumer =
            DrawerConsumer().setDrawerView(SwipeConsumer.DIRECTION_RIGHT, rightSlideView)
        rightDrawerConsumer.setEdgeSize(SmartSwipe.dp2px(40, this))
        rightDrawerConsumer.setShadowColor(Color.parseColor("#9e9e9e"))
        rightDrawerConsumer.setShadowSize(SmartSwipe.dp2px(8, this))
        SmartSwipe.wrap(this).addConsumer(rightDrawerConsumer)
        exchangeButton = findViewById(R.id.main_slide_right_exchange_button)
        exchangeButton.setOnClickListener(View.OnClickListener { view: View? ->
            val checkedSourceLanguage = getCheckedList(sourceList)[0]
            val checkedTargetLanguages = getCheckedList(targetList)
            val checkedTargetLanguage: LanguageBean
            if (checkedTargetLanguages.isEmpty()) {
                disSelectList(targetList)
                targetList[checkedSourceLanguage.userData.toInt()].setIsSelected(true)
                sourceList[checkedSourceLanguage.userData.toInt()].setIsSelected(false)
                sourceList[Consts.LANGUAGE_AUTO.toInt()].setIsSelected(true)
                rightSourceRv.updateData()
                rightTargetRv.updateData()
                //					targetList.get(checkedSourceLanguage)
//					return;
            } else {
                val mapping = rightTargetRv.adapter?.mapping
                if (mapping != null) {
                    for (each in mapping) {
                        if (targetList[each].isSelected) {
                            checkedTargetLanguage = targetList[each]
                            disSelectList(targetList)
                            targetList[checkedSourceLanguage.userData.toInt()].setIsSelected(true)
                            sourceList[checkedSourceLanguage.userData.toInt()].setIsSelected(false)
                            sourceList[checkedTargetLanguage.userData.toInt()].setIsSelected(true)
                            rightSourceRv.updateData()
                            rightTargetRv.updateData()
                            break
                        }
                    }
                }
            }
            Log.i(TAG, "click button!")
        })
        val copyrightInfo = "Copyright@FunnySaltyFish\n2020\nAll Rights Reserved"
        copyrightTextView = findViewById(R.id.main_slide_right_copyright)
        val ssb = SpannableStringBuilder()
        ssb.append(copyrightInfo)
        val sc: ClickableSpan = object : ClickableSpan() {
            override fun onClick(p1: View) {
                // TODO: Implement this method
                val url =
                    "https://imgconvert.csdnimg.cn/aHR0cHM6Ly9hZTAxLmFsaWNkbi5jb20va2YvSGJmZmU2OWQyNTE0ZDRhZjhhZDBmYzgwNjY4YzU3ZDU0US5wbmc?x-oss-process=image/format,png"
                val url2 = "https://i.loli.net/2020/04/05/PBtK4pv5nR6Cdme.png"
                val intent = Intent()
                val uri: Uri = if (Math.random() < 0.5) {
                    Uri.parse(url)
                } else {
                    Uri.parse(url2)
                }
                intent.action = Intent.ACTION_VIEW
                intent.data = uri
                this@MainActivity.startActivity(intent)
            }
        }
        val start = copyrightInfo.indexOf("FunnySaltyFish")
        val end = start + "FunnySaltyFish".length
        ssb.setSpan(sc, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        val foregroundColorSpan = ForegroundColorSpan(Color.parseColor("#2196f3"))
        ssb.setSpan(foregroundColorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        //配置给TextView
        copyrightTextView.movementMethod = LinkMovementMethod.getInstance()
        copyrightTextView.text = ssb
        //		exchangeButton.setLayoutParams(tvParam);
    }

    private fun createWordCompletePopup() {
        //wordCompletePopup = new WordCompletePopup(this,null);
    }

    private fun disSelectList(list: ArrayList<LanguageBean>) {
        for (item in list) {
            item.setIsSelected(false)
        }
    }

    private fun initPreferenceDataRightRv() {
        disSelectList(sourceList)
        disSelectList(targetList)
        disSelectList(engineList)
        disSelectList(ttsList)
        disSelectList(modeList)
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val checkedSourceLanguage =
            sp.getString("preference_language_source_default", "1")!!.toInt()
        sourceList[checkedSourceLanguage].setIsSelected(true)
        //Log.i(TAG,"____获取到的checkedSourceL is "+checkedSourceLanguage);
        val checkedTargetLanguages =
            sp.getStringSet("preference_language_target_default", HashSet())!!.toTypedArray()
        if (checkedTargetLanguages.isNotEmpty()) {
            for (checkedTargetLanguage in checkedTargetLanguages) {
                targetList[checkedTargetLanguage.toInt()].setIsSelected(true)
                //Log.i(TAG,"____获取到的checkedTargetL 包括 "+checkedTargetLanguages[i]);
            }
        } else {
            targetList[2].setIsSelected(true)
        }
        val checkedEngines = sp.getStringSet("preference_engines_default", HashSet())!!.toTypedArray()
        if (checkedEngines.isNotEmpty()) {
            for (checkedEngine in checkedEngines) {
                engineList[checkedEngine.toInt()].setIsSelected(true)
            }
        } else {
            engineList[Consts.ENGINE_GOOGLE.toInt()].setIsSelected(true)
            engineList[Consts.ENGINE_YOUDAO_NORMAL.toInt()].setIsSelected(true)
        }
        val pre_language_mapping_string =
            SharedPreferenceUtil.getInstance().getString("pre_language_mapping_string", "")
        val languageMapping: IntArray
        if (pre_language_mapping_string == "") {
            languageMapping = IntArray(Consts.LANGUAGES.size)
            DataUtil.setDefaultMapping(languageMapping)
        } else {
            languageMapping = DataUtil.coverStringToIntArray(pre_language_mapping_string)
        }
        rightSourceRv.initData(sourceList, languageMapping)
        rightTargetRv.initData(targetList, languageMapping)
        val engineMapping = IntArray(Consts.ENGINE_NAMES.size)
        DataUtil.setDefaultMapping(engineMapping)
        rightEngineRv.initData(engineList, engineMapping)
        val ttsMapping = IntArray(Consts.TTS_NAMES.size)
        DataUtil.setDefaultMapping(ttsMapping)
        rightTTSRv.initData(ttsList, ttsMapping)
        val modeMapping = IntArray(Consts.MODE_NAMES.size)
        DataUtil.setDefaultMapping(modeMapping)
        rightModeRv.initData(modeList, modeMapping)

//		int[] jsMapping = new int[MODE_NAMES.length];
//		DataUtil.setDefaultMapping(jsMapping);
//		rightJSRv.initData(jsList,jsMapping);
        ttsList[Consts.TTS_BAIDU.toInt()].setIsSelected(true)
        modeList[Consts.MODE_NORMAL.toInt()].setIsSelected(true)
    }

    private fun initPreferenceDataDiyBaidu() {
        //百度翻译 自定义
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        Log.i(TAG, "是否使用自定义百度？" + sp.getBoolean("preference_baidu_is_diy_api", false))
        if (sp.getBoolean("preference_baidu_is_diy_api", false)) {
            Consts.BAIDU_APP_ID =
                sp.getString("preference_baidu_diy_appid", Consts.DEFAULT_BAIDU_APP_ID)
            Consts.BAIDU_SECURITY_KEY =
                sp.getString("preference_baidu_diy_key", Consts.DEFAULT_BAIDU_SECURITY_KEY)
            Consts.BAIDU_SLEEP_TIME = sp.getString(
                    "preference_baidu_diy_sleep_time",
                    Consts.DEFAULT_BAIDU_SLEEP_TIME.toString()
                )!!.toLong()
        } else {
            Consts.BAIDU_APP_ID = Consts.DEFAULT_BAIDU_APP_ID
            Consts.BAIDU_SECURITY_KEY = Consts.DEFAULT_BAIDU_SECURITY_KEY
            Consts.BAIDU_SLEEP_TIME = Consts.DEFAULT_BAIDU_SLEEP_TIME
        }
    }

    private fun initSwipeListener() {
        swipeListener = object : SimpleSwipeListener() {
            override fun onSwipeOpened(
                wrapper: SmartSwipeWrapper,
                consumer: SwipeConsumer,
                direction: Int
            ) {
                if (consumer.javaClass == rightDrawerConsumer.javaClass) {
                    //如果打开的是右边的
                    if (leftSlidingConsumer.isOpened) {
                        leftSlidingConsumer.smoothClose()
                    }
                } else if (consumer.javaClass == leftSlidingConsumer.javaClass) {
                    if (rightDrawerConsumer.isOpened) {
                        rightDrawerConsumer.smoothClose()
                    }
                }
                super.onSwipeOpened(wrapper, consumer, direction)
            }

            override fun onSwipeProcess(
                wrapper: SmartSwipeWrapper,
                consumer: SwipeConsumer,
                direction: Int,
                settling: Boolean,
                progress: Float
            ) {
                if (consumer.javaClass == rightDrawerConsumer.javaClass) {
                    //如果打开的是右边的
                    if (leftSlidingConsumer.isOpened) {
                        leftSlidingConsumer.close()
                    }
                } else if (consumer.javaClass == leftSlidingConsumer.javaClass) {
                    if (rightDrawerConsumer.isOpened) {
                        rightDrawerConsumer.smoothClose()
                    }
                }
                super.onSwipeProcess(wrapper, consumer, direction, settling, progress)
            }

            override fun onSwipeClosed(
                wrapper: SmartSwipeWrapper,
                consumer: SwipeConsumer,
                direction: Int
            ) {
                super.onSwipeClosed(wrapper, consumer, direction)
            }
        }
        leftSlidingConsumer.addListener(swipeListener)
        rightDrawerConsumer.addListener(swipeListener)
    }

    private fun initTasks(content: String) {
        tasks.clear()

        resultAdapter.data.clear()
        resultAdapter.notifyDataSetChanged()

        val source = getCheckedList(sourceList)[0].userData
        var task: BasicTranslationTask
        for (target in getCheckedList(targetList)) {
            for (engine in getCheckedList(engineList)) {
                task = when (engine.userData) {
                    Consts.ENGINE_JINSHAN ->
                        TranslationJinshanEasy(content, source, target.userData)
                    Consts.ENGINE_YOUDAO_NORMAL -> TranslationYouDaoNormal(
                        content,
                        source,
                        target.userData
                    )
                    Consts.ENGINE_BAIDU_NORMAL -> TranslationBaiduNormal(
                        content,
                        source,
                        target.userData
                    )
                    Consts.ENGINE_GOOGLE -> TranslationGoogleNormal(
                        content,
                        source,
                        target.userData
                    )
                    Consts.ENGINE_BV_TO_AV -> TranslationBV2AV(content, source, target.userData)
                    Consts.ENGINE_BIGGER_TEXT -> TranslationBiggerText(
                        content,
                        source,
                        target.userData
                    )
                    Consts.ENGINE_EACH_TEXT -> TranslationEachText(content, source, target.userData)
                    else -> throw IllegalStateException("没有这个引擎: " + engine.userData)
                }
                //task.setId(i++);
                //task.setLisener(helper.defaultListener);
                tasks.add(task)
            }

            //处理插件
            for (bean in getCheckedList(jsList)) {
                val jsLanguageBean = bean as JSLanguageBean
                val jsEngine = getJsEngineById(jsLanguageBean.id)
                if (jsEngine != null) {
                    val jsTranslateTask =
                        JsTranslateTask(jsEngine, content, source, target.userData)
                    tasks.add(jsTranslateTask)
                }
            }
        }
    }

    private fun getCheckedList(list: ArrayList<LanguageBean>): ArrayList<LanguageBean> {
        val result = ArrayList<LanguageBean>()
        for (bean in list) {
            if (bean.isSelected) {
                result.add(bean)
            }
        }
        return result
    }

    private fun initBitmaps() {
        //IC_SINGLE_CHECK_CHECKED=BitmapUtil.getBitmapFromResources();
        val checkColor = Color.parseColor("#2196f3")
        val checkSize = 24
        Consts.IC_SINGLE_CHECK_CHECKED =
            getIcon(R.drawable.ic_single_check_checked, checkSize, checkSize, checkColor)
        Consts.IC_SINGLE_CHECK =
            getIcon(R.drawable.ic_single_check, checkSize, checkSize, checkColor)
        Consts.IC_MULTI_CHECK = getIcon(R.drawable.ic_multi_check, checkSize, checkSize, checkColor)
        Consts.IC_MULTI_CHECK_CHECKED =
            getIcon(R.drawable.ic_multi_check_checked, checkSize, checkSize, checkColor)
        Consts.IC_MENU_RIGHT_ARROW = getIcon(R.drawable.ic_menu_right_arrow, Color.WHITE)
    }

    private fun getIcon(id: Int, color: Int): Drawable {
        val bitmap = BitmapUtil.getBitmapFromResources(re, id)
        val b = BitmapDrawable(resources,bitmap)
        b.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        return b
    }

    private fun getIcon(id: Int, targetWidth: Int, targetHeight: Int, color: Int): Drawable {
        val bitmap = BitmapUtil.getBigBitmapFromResources(re, id, targetWidth, targetHeight)
        val b = BitmapDrawable(resources,bitmap)
        b.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        return b
    }

    //print("statuebarh:"+height);
    private val statusBarHeight: Int
        get() {
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            //print("statuebarh:"+height);
            return resources.getDimensionPixelSize(resourceId)
        }
    private val checkedTTSEngine: Short
        get() {
            val curBean = getCheckedList(ttsList)[0]
            return curBean.userData
        }

    private fun createDialogs() {
        if (ApplicationUtil.isFirstOpen(this)) {
            showMessageDialog(
                title = "Welcome",
                message = readAssets("introduction.md"),
                isMarkdown = true,
                positiveText = "我知道了",
                positiveAction = {
                    ApplicationUtil.print(this@MainActivity, "愉快玩耍吧！")
                },
                negativeText = "找作者",
                negativeAction = {
                    val url =
                        "https://imgconvert.csdnimg.cn/aHR0cHM6Ly9hZTAxLmFsaWNkbi5jb20va2YvSGJmZmU2OWQyNTE0ZDRhZjhhZDBmYzgwNjY4YzU3ZDU0US5wbmc?x-oss-process=image/format,png"
                    val url2 = "https://i.loli.net/2020/04/05/PBtK4pv5nR6Cdme.png"
                    val intent = Intent()
                    val uri: Uri = if (Math.random() < 0.5) {
                        Uri.parse(url)
                    } else {
                        Uri.parse(url2)
                    }
                    intent.action = Intent.ACTION_VIEW
                    intent.data = uri
                    this@MainActivity.startActivity(intent)
                }
            )
        }
    }

    fun showUpdateDialog() {
        if (UpdateUtil.isUpdate) {
            val updateDialog = AlertDialog.Builder(this)
                .setTitle("有新版！")
                .setMessage(UpdateUtil.getUpdateLog())
                .setPositiveButton("立即更新") { p1, p2 -> // TODO: Implement this method
                    UpdateUtil.startUpdateByBrowse(this@MainActivity, UpdateUtil.getApkUrl())
                    ApplicationUtil.print(this@MainActivity, "正在为您跳转……")
                }
                .setNegativeButton("稍后再说") { p1, p2 -> // TODO: Implement this method
                    ApplicationUtil.copyToClipboard(this@MainActivity, UpdateUtil.getApkUrl())
                    ApplicationUtil.print(this@MainActivity, "已为您复制Apk链接，您可以稍后自行下载！")
                }
                .setCancelable(false)
                .create()
            updateDialog.show()
        } else {
            val updateDialog = AlertDialog.Builder(this)
                .setTitle("公告！")
                .setMessage(UpdateUtil.getUpdateLog())
                .setPositiveButton("我知道了") { p1: DialogInterface?, p2: Int -> }
                .setCancelable(false)
                .create()
            updateDialog.show()
        }
        //不执行后面的
    }

    //判断本软件是否处于空闲状态
    val isFree: Boolean
        get() { //判断本软件是否处于空闲状态
            return !(isFinishing || isDestroyed || isBackground)
        }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        // TODO: Implement this method
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            if (!rightDrawerConsumer.isClosed) { //如果打开了右侧侧滑，就先关闭
                rightDrawerConsumer.smoothClose()
                return true
            }
            if (leftSlidingConsumer.isOpened) {
                leftSlidingConsumer.smoothClose()
                return true
            }
            curBackTime = System.currentTimeMillis()
            if (curBackTime - firstBackTime > 2000) {
                ApplicationUtil.print(this, "这位童鞋，再按一次退出程序哦(´-ω-`)")
                firstBackTime = curBackTime
            } else {
                finish()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val menuItem = menu.getItem(0)
        menuItem.icon = Consts.IC_MENU_RIGHT_ARROW
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_main_right_arrow -> {
                if (rightDrawerConsumer.isClosed) {
                    rightDrawerConsumer.smoothRightOpen()
                } else {
                    rightDrawerConsumer.smoothClose()
                }
                true
            }
            android.R.id.home -> {
                if (leftSlidingConsumer.isOpened) {
                    leftSlidingConsumer.smoothClose()
                    //item.setIcon(R.drawable.ic_menu);
                } else {
                    leftSlidingConsumer.smoothLeftOpen()
                    //item.setIcon(R.drawable.ic_menu_narrow_left);
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        isBackground = true
    }

    override fun onResume() {
        super.onResume()
        isBackground = false
    }

    override fun onDestroy() {
        isBackground = true
        TTSUtil.destroyTTS()
        FunnyApplication.getProxy(this).shutdown()
        dbEnglishWordsUtils.close()
        dbJSUtils.close()
        NewTranslationHelper.getInstance().finish()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //Log.i(TAG,"onActivityResult。收到的requestCode:"+requestCode+"  resultCode is"+resultCode);
        if (requestCode == Consts.ACTIVITY_MAIN && resultCode == Consts.ACTIVITY_SETTING) {
            val isRvChange =
                data?.getBooleanExtra("isRvChange", false) //targetList、sourceList内容是否改变
            val isDiyBaiduChange = data?.getBooleanExtra("isDiyBaiduChange", false) //是否自定义百度
            //Log.i(TAG,"_____获取到传回的isRvChange额为:"+isRvChange);
            if (isRvChange!!) initPreferenceDataRightRv()
            if (isDiyBaiduChange!!) initPreferenceDataDiyBaidu()
        } else if (requestCode == Consts.ACTIVITY_MAIN && resultCode == Consts.ACTIVITY_JS_MANAGE) {
            assert(data != null)
            val hasChanged = data?.getBooleanExtra("hasChanged", false)
            if (hasChanged!!) {
                clear()
                initJsEngine()
            }
        }
    }
}