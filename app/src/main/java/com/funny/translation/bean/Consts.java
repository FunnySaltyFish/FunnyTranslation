package com.funny.translation.bean;
import android.graphics.drawable.Drawable;

public class Consts
{
	public static String ERROR_UNKNOWN="未知错误！翻译失败！";
	public static String ERROR_JSON="Json数据解析错误！";
	public static String ERROR_UNSUPPORT_LANGUAGE="暂不支持的语言翻译形式";
	public static String ERROR_DATED_ENGINE="当前方法已过期！请反馈！";
	
	public final static short ENGINE_YOUDAO_EASY=0;
	public final static short ENGINE_YOUDAO_NORMAL=1;
	public final static short ENGINE_BAIDU_NORMAL=2;
	public final static short ENGINE_GOOGLE=3;
	
	public final static short TTS_LOCAL=0;
	public final static short TTS_BAIDU=1;
	
	public final static short LANGUAGE_AUTO=0;
	public final static short LANGUAGE_CHINESE=1;
	public final static short LANGUAGE_ENGLISH=2;
	public final static short LANGUAGE_JAPANESE=3;
	public final static short LANGUAGE_KOREAN=4;
	public final static short LANGUAGE_FRENCH=5;
	public final static short LANGUAGE_RUSSIAN=6;
	public final static short LANGUAGE_GERMANY=7;
	public final static short LANGUAGE_WENYANWEN=8;
	
	public static String[][] LANGUAGES={
		//有道翻译 简/标准 百度翻译 标准
		{"auto","auto","auto","auto"},
		{"ZH_CN","cn","zh","zh-CN"},
		{"EN","en","en","en"},
		{"JA","ja","jp","ja"},
		{"KR","ko","kor","ko"},
		{"FR","fr","fra","fr"},
		{"RU","ru","ru","ru"},
		{"DE","de","de","de"},
		{"ZH_CN","cn","wyw","zh-CN"}
	};
	
	public final static short CHECK_SINGLE=0;
	public final static short CHECK_MULTI=1;
	
	public static String[] LANGUAGE_NAMES;
	
	public static String[] ENGINE_NAMES;
	
	public static String[] TTS_NAMES;
	
	//百度翻译常量
	public static final String BAIDU_APP_ID = "20200127000378153";
    public static final String BAIDU_SECURITY_KEY = "DXWRr6JKdNPlhl5M63GX";
	
	public static Drawable IC_MULTI_CHECK;
	public static Drawable IC_MULTI_CHECK_CHECKED;
	public static Drawable IC_SINGLE_CHECK;
	public static Drawable IC_SINGLE_CHECK_CHECKED;
	public static Drawable IC_MENU_RIGHT_ARROW;
}
