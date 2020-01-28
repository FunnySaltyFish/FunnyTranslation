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
	
	public final static short LANGUAGE_AUTO=0;
	public final static short LANGUAGE_CHINESE=1;
	public final static short LANGUAGE_ENGLISH=2;
	public final static short LANGUAGE_JAPANESE=3;
	public final static short LANGUAGE_KOREAN=4;
	public final static short LANGUAGE_FRENCH=5;
	public final static short LANGUAGE_RUSSIAN=6;
	public final static short LANGUAGE_GERMANY=7;
	
	public static String[][] LANGUAGES={
		//有道翻译 简/标准 百度翻译 标准
		{"auto","auto","auto"},
		{"cn","cn","zh"},
		{"en","en","en"},
		{"ja","ja","jp"},
		{"ko","ko","kor"},
		{"fr","fr","fra"},
		{"ru","ru","ru"},
		{"de","de","de"}
	};
	
	public final static short CHECK_SINGLE=0;
	public final static short CHECK_MULTI=1;
	
	public static String[] LANGUAGE_NAMES={};
	
	public static String[] ENGINE_NAMES={
		"有道翻译（简版)（仅中英短译)","有道翻译（标准版)","百度翻译(标准版)"
	};
	
	//百度翻译常量
	public static final String BAIDU_APP_ID = "20200127000378153";
    public static final String BAIDU_SECURITY_KEY = "DXWRr6JKdNPlhl5M63GX";
	
	public static Drawable IC_MULTI_CHECK;
	public static Drawable IC_MULTI_CHECK_CHECKED;
	public static Drawable IC_SINGLE_CHECK;
	public static Drawable IC_SINGLE_CHECK_CHECKED;
}
