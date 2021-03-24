package com.funny.translation.bean;
import android.graphics.drawable.Drawable;

import com.funny.translation.utils.RC4;

public class Consts
{


	//抽象话词库
	//https://github.com/gaowanliang/NMSL/blob/master/src/data/emoji.json
	//bing
	//https://cn.bing.com/dict/search?q=%E5%8D%95%E8%AF%8D
	//有道朗读
	//http://dict.youdao.com/dictvoice?audio=good

	public static String AUTHOR="FunnySaltyFish";

	public static final int ACTIVITY_MAIN=1;
	public static final int ACTIVITY_SETTING=2;
	public static final int ACTIVITY_FEEDBACK=3;
	public static final int ACTIVITY_JS_MANAGE=3;

	public static final String ERROR_UNKNOWN="未知错误！翻译失败！";
	public static final String ERROR_JSON="Json数据解析错误！";
	public static final String ERROR_UNSUPPORT_LANGUAGE="暂不支持的语言翻译形式";
	public static final String ERROR_DATED_ENGINE="当前方法已过期！请反馈！";
	public static final String ERROR_POST = "发送POST请求异常！请检查网络连接！";
	public static final String ERROR_IO="IO流错误！";
	public static final String ERROR_ILLEGAL_DATA = "数据不合法！";
	public static final String ERROR_ONLY_CHINESE_SUPPORT = "当前翻译模式仅支持中文！";
	public static final String ERROR_NO_BV_OR_AV = "未检测到有效的Bv号或Av号";

	//public final static short ENGINE_YOUDAO_EASY=0;
	public static final short ENGINE_JINSHAN = 0;
	public final static short ENGINE_YOUDAO_NORMAL=1;
	public final static short ENGINE_BAIDU_NORMAL=2;
	public final static short ENGINE_GOOGLE=3;
	public static final short ENGINE_BV_TO_AV=4;//Bv，Av互转
	public static final short ENGINE_BIGGER_TEXT = 5;//字符放大
	public static final short ENGINE_EACH_TEXT = 6;//逐字翻译

	public static final short ENGINE_JS = 100;
	
	public final static short TTS_LOCAL=0;
	public final static short TTS_BAIDU=1;
	public final static short TTS_GOOGLE=2;
	public static final short TTS_YOUDAO = 2;

	public final static short LANGUAGE_AUTO=0;
	public final static short LANGUAGE_CHINESE=1;
	public final static short LANGUAGE_ENGLISH=2;
	public final static short LANGUAGE_JAPANESE=3;
	public final static short LANGUAGE_KOREAN=4;
	public final static short LANGUAGE_FRENCH=5;
	public final static short LANGUAGE_RUSSIAN=6;
	public final static short LANGUAGE_GERMANY=7;
	public final static short LANGUAGE_WENYANWEN=8;
	public final static short LANGUAGE_THAI=9;

	public static final short MODE_NORMAL = 0;//普通翻译模式
	public static final short MODE_EACH_LINE=1;//逐行翻译
	public static final short MODE_EACH_TEXT = 2;//逐字翻译

	public static final int MAX_JS_NUMBER = 20;


	public static String[][] LANGUAGES={
		//有道翻译 简版（已弃用，改为金山）/标准 百度翻译 标准 谷歌(短)
		{"auto","auto","auto","auto"},
		{"zh","zh-CHS","zh","zh-CN"},
		{"en-US","en","en","en"},
		{"ja","ja","jp","ja"},
		{"ko","ko","kor","ko"},
		{"fr","fr","fra","fr"},
		{"ru","ru","ru","ru"},
		{"de","de","de","de"},
		{"zh","zh-CHS","wyw","zh-CN"},
		{"th","th","th","th"}
	};
	
	public final static short CHECK_SINGLE=0;
	public final static short CHECK_MULTI=1;
	
	public static String[] LANGUAGE_NAMES;
	
	public static String[] ENGINE_NAMES;
	
	public static String[] TTS_NAMES;

	public static String[] MODE_NAMES;

	//百度翻译常量
	public static String DEFAULT_BAIDU_APP_ID = RC4.decry_RC4("785ebf34dc6aa09ffc4f5726d7bcb14f3f","27420");
	public static String DEFAULT_BAIDU_SECURITY_KEY = RC4.decry_RC4("0e36da569e6dd8e3a831377988e8b5373ab87173","27420");
	public static Long DEFAULT_BAIDU_SLEEP_TIME = 800L;

	public static String BAIDU_APP_ID = "";
    public static String BAIDU_SECURITY_KEY = "";
	public static Long BAIDU_SLEEP_TIME = 800L;

	public static Drawable IC_MULTI_CHECK;
	public static Drawable IC_MULTI_CHECK_CHECKED;
	public static Drawable IC_SINGLE_CHECK;
	public static Drawable IC_SINGLE_CHECK_CHECKED;
	public static Drawable IC_MENU_RIGHT_ARROW;

	public final static int MESSAGE_FINISH_CURRENT_TASK = 0x001;
	public final static int MESSAGE_FINISH_ALL_TASKS = 0x002;
	
	public final static short SKIN_BLUE=0;
	public final static short SKIN_GREEN=1;
	public final static short SKIN_BLACK=2;
}
