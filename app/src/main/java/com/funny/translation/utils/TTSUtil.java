package com.funny.translation.utils;
import android.speech.tts.TextToSpeech;
import android.content.Context;
import java.util.Locale;
import com.funny.translation.bean.Consts;
import android.media.MediaPlayer;
import java.io.IOException;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.io.File;
import com.funny.translation.FunnyApplication;
import android.support.v7.app.AppCompatActivity;
import com.danikula.videocache.HttpProxyCacheServer;

public class TTSUtil
{
	public static HttpProxyCacheServer proxy;
	
	public static TextToSpeech localTTS=null;
	public static MediaPlayer internetTTS=null;
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
	
	public static void initInternet(final Context ctx){
		if(internetTTS==null){
			AppCompatActivity activity=(AppCompatActivity)ctx;
			FunnyApplication application=(FunnyApplication)activity.getApplication();
			proxy=application.getProxy(ctx);
			internetTTS=new MediaPlayer();
			internetTTS.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){
					@Override
					public void onPrepared(MediaPlayer player)
					{
						// TODO: Implement this method
						player.seekTo(0);
						player.start();
					}
			});
			internetTTS.setOnErrorListener(new MediaPlayer.OnErrorListener(){
					@Override
					public boolean onError(MediaPlayer p1, int p2, int p3)
					{
						// TODO: Implement this method
						//System.out.printf("出错了！p2:%d p3:%d\n",p2,p3);
						if(p2==1&&p3==-2147483648){
							ApplicationUtil.print(ctx,"当前调用的TTS引擎可能暂不支持此语种！");
						}
						return false;
					}
			});
		}
	}
	
	public static void rePlayInternet(Context ctx,String url){
		try{
			if(internetTTS!=null&&internetTTS.isPlaying()){
				internetTTS.stop();
			}
			internetTTS.reset();
			//File f=HttpUtil.downloadFile("url","/data/media/");
			//internetTTS.setDataSource(f);
			internetTTS.setDataSource(proxy.getProxyUrl(url));
			internetTTS.prepareAsync();
		}catch(IOException e){
			ApplicationUtil.print(ctx,"IO流错误！");
			e.printStackTrace();
		}
	}
	
	public static void playOrPauseInternet(){
		if(internetTTS.isPlaying()){
			internetTTS.pause();
		}else{
			internetTTS.start();
		}
	}
	
	public static String getInternetUrl(short TTSEngine,String text,short language){
		String url=null;
		switch(TTSEngine){
			case Consts.TTS_BAIDU:
				try
				{
					//文言文处理为中文
					String lANGUAGE =(language==Consts.LANGUAGE_WENYANWEN?Consts.LANGUAGES[Consts.LANGUAGE_CHINESE][Consts.ENGINE_BAIDU_NORMAL]:Consts.LANGUAGES[language][Consts.ENGINE_BAIDU_NORMAL]);
					url =// "http://tts.baidu.com/text2audio?lan="+Consts.LANGUAGES[language][Consts.ENGINE_BAIDU_NORMAL]+"&ie=UTF-8&text=" + URLEncoder.encode(text, "utf-8");
						"https://tts.baidu.com/text2audio?tex=" + URLEncoder.encode(text, "utf-8") + "&cuid=baike&ctp=1&pdt=301&vol=9&rate=32&per=0&lan=" + lANGUAGE;
					//url="https://sq-sycdn.kuwo.cn/dadfabca52e99d6c72c1669ac14c52dd/5e401c6e/resource/a2/64/96/3621236540.aac";
					//System.out.println("url"+url);
				}
				catch(Exception e)
				//catch (UnsupportedEncodingException e)
				{
					e.printStackTrace();
				}
				break;
		}
		return url;
	}
	
	
	public static void speak(Context ctx,String text,short language,short TTSEngine){
		if (TTSEngine == Consts.TTS_LOCAL){
			if (localTTS == null)
			{
				initLocal(ctx);
				ApplicationUtil.print(ctx,"正在初始化本地朗读引擎……\n如需朗读请稍等再次点击");
				return;
			}
			int result=localTTS.setLanguage(getLanguage(language));
			if (result != TextToSpeech.LANG_COUNTRY_AVAILABLE
				&& result != TextToSpeech.LANG_AVAILABLE)
			{
				ApplicationUtil.print(ctx, "您的本地TTS暂不支持该种语言的朗读");
				return;
			}
			localTTS.speak(text, TextToSpeech.QUEUE_ADD, null);
		}else{//网络
			if(internetTTS==null){
				initInternet(ctx);
				ApplicationUtil.print(ctx,"正在初始化网络朗读引擎……\n如需朗读请稍等再次点击");
				return;
			}
			rePlayInternet(ctx,getInternetUrl(TTSEngine,text,language));
		}
	}
	
	public static Locale getLanguage(short targetLanguage){
		Locale language;
		switch(targetLanguage){
			case Consts.LANGUAGE_CHINESE:
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
			case Consts.LANGUAGE_WENYANWEN:
				language=Locale.CHINESE;
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
		if(internetTTS!=null){
			if(internetTTS.isPlaying())internetTTS.stop();
			internetTTS.release();
			internetTTS=null;
		}
	}
}
