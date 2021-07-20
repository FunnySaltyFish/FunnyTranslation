package com.funny.translation.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.speech.tts.TextToSpeech;

import com.danikula.videocache.HttpProxyCacheServer;
import com.funny.translation.MainActivity;
import com.funny.translation.bean.Consts;
import com.funny.translation.thread.InternetTTSThread;

import java.util.Locale;

public class TTSUtil
{
	public static HttpProxyCacheServer proxy;
	
	public static TextToSpeech localTTS=null;
	public static MediaPlayer internetTTS=null;
	public static InternetTTSThread internetTTSThread;
	public static void initLocal(Context ctx){
		localTTS = new TextToSpeech(ctx, new TextToSpeech.OnInitListener(){
				@Override
				public void onInit(int status)
				{
					// TODO: Implement this method
					if(status!=TextToSpeech.SUCCESS){
						localTTS=null;
					}
				}
		});
	}
	
	public static void initInternet(final Context ctx,Handler handler){
		internetTTSThread=new InternetTTSThread(ctx,handler);
		internetTTSThread.setName("InternetTTSThread");
		internetTTSThread.start();
	}
	
	public static void rePlayInternet(Context ctx,String url){
		try{
			if(internetTTSThread!=null&&internetTTSThread.isAlive()){
				internetTTSThread.setAddUrl(url);
			}
		}catch(Exception e){
			ApplicationUtil.print(ctx,"朗读发生错误！");
			e.printStackTrace();
		}
	}
	
	public static void playOrPauseInternet(){
		if(internetTTSThread!=null&&internetTTSThread.isAlive()){
			internetTTSThread.playOrPause();
		}
	}
	
	public static String getInternetUrl(short TTSEngine,String text,short language){
		String url=null;
		if(text.equals("0c1be36a95")){
			url = "funny://egg_1_4";
			return url;
		}
		switch(TTSEngine){
			case Consts.TTS_BAIDU:
				try
				{
					//文言文处理为中文
					String lANGUAGE =(language==Consts.LANGUAGE_WENYANWEN?Consts.LANGUAGES[Consts.LANGUAGE_CHINESE][Consts.ENGINE_BAIDU_NORMAL]:Consts.LANGUAGES[language][Consts.ENGINE_BAIDU_NORMAL]);
					//	"https://tts.baidu.com/text2audio?tex=" + URLEncoder.encode(text, "utf-8") + "&cuid=baike&ctp=1&pdt=301&vol=9&rate=32&per=0&lan=" + lANGUAGE;
					url=String.format("https://fanyi.baidu.com/gettts?lan=%s&text=%s&spd=3&source=wise",lANGUAGE,android.net.Uri.encode(text));//将 转为%20而不是加号
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				break;
			case Consts.TTS_YOUDAO:
				url = String.format("http://dict.youdao.com/dictvoice?audio=%s", android.net.Uri.encode(text));
				break;
//			case Consts.TTS_GOOGLE:
//				try
//				{
//					//文言文处理为中文
//					String lANGUAGE =(language==Consts.LANGUAGE_WENYANWEN?Consts.LANGUAGES[Consts.LANGUAGE_CHINESE][Consts.ENGINE_GOOGLE]:Consts.LANGUAGES[language][Consts.ENGINE_GOOGLE]);
//					url=String.format("https://translate.google.cn/translate_tts?ie=UTF-8&q=%s&tl=%s&total=1&idx=0&textlen=%d&tk=%s&client=webapp",android.net.Uri.encode(text),lANGUAGE,text.length(),FunnyGoogleApi.tk(text,"439500.3343569631"));//将 转为%20而不是加号
//					//System.out.println("url"+url);
//				}
//				catch(Exception e)
//				{
//					e.printStackTrace();
//				}
//				break;
		}
		return url;
	}
	
	
	public static void speak(Context ctx,String text,short language,short TTSEngine){
		if (TTSEngine == Consts.TTS_LOCAL){
			if (localTTS == null) {
				initLocal(ctx);
//				ApplicationUtil.print(ctx,"正在初始化本地朗读引擎……\n如需朗读请稍等再次点击");
//				return;
			}
			int result=localTTS.setLanguage(getLanguage(language));
			if (result != TextToSpeech.LANG_COUNTRY_AVAILABLE
				&& result != TextToSpeech.LANG_AVAILABLE) {
				ApplicationUtil.print(ctx, "您的本地TTS暂不支持该种语言的朗读");
				return;
			}
			localTTS.speak(text, TextToSpeech.QUEUE_ADD, null);
		}else{//网络
			if(internetTTSThread==null){
				MainActivity activity=(MainActivity)ctx;
				initInternet(ctx,activity.getHandler());
//				ApplicationUtil.print(ctx,"正在初始化网络朗读引擎……\n如需朗读请稍等再次点击");
//				return;
			}
			rePlayInternet(ctx,getInternetUrl(TTSEngine,text,language));
		}
	}
	
	public static Locale getLanguage(short targetLanguage){
		Locale language;
		switch(targetLanguage){
			case Consts.LANGUAGE_CHINESE:
			case Consts.LANGUAGE_WENYANWEN:
				language=Locale.CHINESE;
				break;
			case Consts.LANGUAGE_ENGLISH:
				language=Locale.ENGLISH;
				break;
			case Consts.LANGUAGE_FRENCH:
				language=Locale.FRENCH;
				break;
			case Consts.LANGUAGE_JAPANESE:
				language=Locale.JAPANESE;
				break;
			case Consts.LANGUAGE_GERMANY:
				language=Locale.GERMANY;
				break;
			case Consts.LANGUAGE_KOREAN:
				language=Locale.KOREAN;
				break;
			default:
				language=Locale.CHINESE;
				break;
		}
		return language;
	}
	
	public static void destroyTTS(){
		if(localTTS!=null){
			localTTS.shutdown();
		}
		if(internetTTSThread!=null){
			internetTTSThread.destroyTTS();
		}
	}
}
