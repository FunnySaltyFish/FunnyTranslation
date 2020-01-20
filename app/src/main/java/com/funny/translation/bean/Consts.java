package com.funny.translation.bean;

public class Consts
{
	public static String UNKNOWN_ERROR="未知错误！翻译失败！";
	public static String JSON_ERROR="Json数据解析错误！";
	
	public final static short ENGINE_YOUDAO_EASY=0;
	public final static short ENGINE_YOUDAO_NORMAL=1;
	public final static short ENGINE_BAIDU=2;
	
	public final static short LANGUAGE_AUTO=0;
	public final static short LANGUAGE_CHINESE=1;
	public final static short LANGUAGE_ENGLISH=2;
	public final static short LANGUAGE_JAPANESE=3;
	
	public static String[][] LANGUAGES={
		//有道翻译 
		{"auto","auto"},
		{"cn","cn"},
		{"en","en"},
		{"ja","ja"}
	};
	public static String[] ENGINE_NAMES={
		"有道翻译（简版)","有道翻译（标准版)","百度翻译"
	};
}
