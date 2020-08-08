package com.funny.translation.bean;
import android.graphics.drawable.Drawable;

public class Consts
{



	//抽象话词库
	//https://github.com/gaowanliang/NMSL/blob/master/src/data/emoji.json
	public static String AUTHOR="FunnySaltyFish";

	public static final int ACTIVITY_MAIN=1;
	public static final int ACTIVITY_SETTING=2;
	public static final int ACTIVITY_FEEDBACK=3;

	public static final String ERROR_UNKNOWN="未知错误！翻译失败！";
	public static final String ERROR_JSON="Json数据解析错误！";
	public static final String ERROR_UNSUPPORT_LANGUAGE="暂不支持的语言翻译形式";
	public static final String ERROR_DATED_ENGINE="当前方法已过期！请反馈！";
	public static final String ERROR_POST = "发送POST请求异常！请检查网络连接！";
	public static final String ERROR_IO="IO流错误！";
	public static final String ERROR_ILLEGAL_DATA = "数据不合法！";
	public static final String ERROR_ONLY_CHINESE_SUPPORT = "当前翻译模式仅支持中文！";
	public static final String ERROR_NO_BV_OR_AV = "未检测到有效的Bv号或Av号";

	public final static short ENGINE_YOUDAO_EASY=0;
	public final static short ENGINE_YOUDAO_NORMAL=1;
	public final static short ENGINE_BAIDU_NORMAL=2;
	public final static short ENGINE_GOOGLE=3;
	public static final short ENGINE_BV_TO_AV=4;//Bv，Av互转
	
	public final static short TTS_LOCAL=0;
	public final static short TTS_BAIDU=1;
	public final static short TTS_GOOGLE=2;
	
	public final static short LANGUAGE_AUTO=0;
	public final static short LANGUAGE_CHINESE=1;
	public final static short LANGUAGE_ENGLISH=2;
	public final static short LANGUAGE_JAPANESE=3;
	public final static short LANGUAGE_KOREAN=4;
	public final static short LANGUAGE_FRENCH=5;
	public final static short LANGUAGE_RUSSIAN=6;
	public final static short LANGUAGE_GERMANY=7;
	public final static short LANGUAGE_WENYANWEN=8;

	public static final short MODE_NORMAL = 0;//普通翻译模式
	public static final short MODE_EACH_LINE=1;//逐行翻译
	public static final short MODE_EACH_TEXT = 2;//逐字翻译


	public static String[][] LANGUAGES={
		//有道翻译 简/标准 百度翻译 标准 谷歌(短)
		{"auto","auto","auto","auto"},
		{"zh-CHS","cn","zh","zh-CN"},
		{"en","en","en","en"},
		{"ja","ja","jp","ja"},
		{"ko","ko","kor","ko"},
		{"fr","fr","fra","fr"},
		{"ru","ru","ru","ru"},
		{"de","de","de","de"},
		{"zh-CHS","cn","wyw","zh-CN"}
	};
	
	public final static short CHECK_SINGLE=0;
	public final static short CHECK_MULTI=1;
	
	public static String[] LANGUAGE_NAMES;
	
	public static String[] ENGINE_NAMES;
	
	public static String[] TTS_NAMES;

	public static String[] MODE_NAMES;
	//百度翻译常量
	public static String DEFAULT_BAIDU_APP_ID = "20200127000378153";
	public static String DEFAULT_BAIDU_SECURITY_KEY = "DXWRr6JKdNPlhl5M63GX";
	public static Long DEFAULT_BAIDU_SLEEP_TIME = 800L;

	public static String BAIDU_APP_ID = "";
    public static String BAIDU_SECURITY_KEY = "";
	public static Long BAIDU_SLEEP_TIME = 800L;

	public static Drawable IC_MULTI_CHECK;
	public static Drawable IC_MULTI_CHECK_CHECKED;
	public static Drawable IC_SINGLE_CHECK;
	public static Drawable IC_SINGLE_CHECK_CHECKED;
	public static Drawable IC_MENU_RIGHT_ARROW;
	
	public final static short SKIN_BLUE=0;
	public final static short SKIN_GREEN=1;
	public final static short SKIN_BLACK=2;
}
