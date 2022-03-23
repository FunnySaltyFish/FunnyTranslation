package com.funny.translation.thread;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.danikula.videocache.HttpProxyCacheServer;
import com.funny.translation.FunnyApplication;
import com.funny.translation.utils.FileUtil;
import com.funny.translation.utils.NetworkUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
public class InternetTTSThread extends Thread
{
	public final static short FLAG_LOADING=1;
	public final static short FLAG_FINISH_ONE=2;
	public final static short FLAG_NO_FILE=3;
	public final static short FLAG_PLAYING=4;
	public final static short FLAG_ADD_URL=5;
	short flag=0;
	public static HttpProxyCacheServer proxy;
	public MediaPlayer internetTTS;
	
	private Context ctx;
	//private ArrayList<String> urls;
	private String curUrl,needAddUrl;
	//private int curUrlId=0;
	private Object lock=new Object();
	
	private Handler handler;
	public InternetTTSThread(Context ctx,Handler handler)
	{
		this.ctx = ctx;
		this.handler=handler;
		//urls=new ArrayList<String>();
		init();
		flag=FLAG_LOADING;
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	@Override
	public void run()
	{
		// TODO: Implement this method
		while(flag>0){
			switch(flag){
				case FLAG_LOADING:
				case FLAG_FINISH_ONE:
					try{
						sleep(100);
					}catch(Exception e){
						e.printStackTrace();
					}
					break;
				case FLAG_PLAYING:
					replay();
					break;
				case FLAG_NO_FILE:
					flag=FLAG_FINISH_ONE;
					break;
				case FLAG_ADD_URL:
					addUrl();
					break;
			}
		}
		super.run();
	}
	
	public void init(){
		if(internetTTS==null){
			AppCompatActivity activity=(AppCompatActivity)ctx;
			proxy= FunnyApplication.getProxy(ctx);
			internetTTS=new MediaPlayer();
			internetTTS.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){
					@Override
					public void onPrepared(MediaPlayer player)
					{
						// TODO: Implement this method
						System.out.println("onPrepared");
						player.seekTo(0);
						player.start();
					}
				});
				
			internetTTS.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
					@Override
					public void onCompletion(MediaPlayer p1)
					{
						// TODO: Implement this method
						//playNext();
						flag=FLAG_FINISH_ONE;
					}
			});
			internetTTS.setOnErrorListener(new MediaPlayer.OnErrorListener(){
					@Override
					public boolean onError(MediaPlayer p1, int p2, int p3)
					{
						// TODO: Implement this method
						//System.out.printf("出错了！p2:%d p3:%d\n",p2,p3);
						if(p2==1&&p3==-2147483648){
							print("当前调用的TTS引擎可能暂不支持此语种！");
						}
						return false;
					}
				});
			flag=FLAG_PLAYING;
		}
	}
	
	public void setAddUrl(String needAddUrl){
		this.needAddUrl=needAddUrl;
		flag=FLAG_ADD_URL;
		//addUrl();
	}
	
	public void addUrl(){
		if(needAddUrl.startsWith("funny://")){//处理彩蛋
			curUrl=needAddUrl;
			flag=FLAG_PLAYING;
			return;
		}
		if(!NetworkUtil.isNetworkConnected(ctx)){//没有网络连接
			if(FileUtil.isCached(ctx,needAddUrl)){
				curUrl=needAddUrl;
				flag=FLAG_PLAYING;
				return;
			}
			else{
				print("当前无网络且朗读音频无缓存！\n请检查您的网络状态！",true);
				flag=FLAG_FINISH_ONE;
				return;
			}
		}
		InputStream ins=null;
		try{
			HttpURLConnection con=(HttpURLConnection)new URL(needAddUrl).openConnection();
			ins=con.getInputStream();
			int length=ins.available();
			/*if(length==0){
				throw new FileNotFoundException();
			}*/
			// else{
				/*synchronized(lock){
					urls.add(needAddUrl);
				}*/
			//}
			curUrl=needAddUrl;
			flag=FLAG_PLAYING;
		}catch(FileNotFoundException e){
			flag=FLAG_NO_FILE;
			print("请检查您的当前翻译结果是否与目标语言相同!\n请不要使用自动选择的结果朗读!",true);
			e.printStackTrace();
		}catch(IOException e){
			flag=FLAG_NO_FILE;
			print("IO流错误!");
			e.printStackTrace();
		}finally{
			if(ins!=null){
				try
				{
					ins.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	@RequiresApi(api = Build.VERSION_CODES.N)
	public void replay(){
		try{
			if(internetTTS!=null&&internetTTS.isPlaying()){
				internetTTS.stop();
			}
			if(curUrl==null){
				return;
			}
			//curUrl=urls.get(curUrlId);
			internetTTS.reset();
			if(curUrl.startsWith("funny://")){
				AssetFileDescriptor fd=ctx.getAssets().openFd("egg1_4.mp3");
				print("恭喜你触发了彩蛋！请认真听哦~");
				internetTTS.setDataSource(fd);
			}else{
				internetTTS.setDataSource(proxy.getProxyUrl(curUrl));
			}
			internetTTS.prepareAsync();
			flag=FLAG_FINISH_ONE;
		}catch(IOException e){
			flag=FLAG_NO_FILE;
			print("IO流错误！");
			e.printStackTrace();
		}
	}
	
	/*public void playNext(){
		if(urls!=null&&curUrlId<urls.size()-1){
			synchronized(lock){
				curUrl=urls.get(++curUrlId);
			}
			replay();
		}else{
			synchronized(lock){
				urls.clear();
			}
			curUrlId=0;
		}
	}*/
	
	public void playOrPause(){
		if(internetTTS.isPlaying()){
			internetTTS.pause();
		}else{
			internetTTS.start();
		}
	}
	
	public void destroyTTS(){
		if(internetTTS!=null){
			if(internetTTS.isPlaying())internetTTS.stop();
			internetTTS.release();
			internetTTS=null;
		}
		flag=-1;
	}
	
	private void print(Object str){
		print(str,false);
	}
	
	private void print(Object str,boolean isLong){
		Message msg=new Message();
		msg.what=0x101;
		msg.obj=str;
		if(isLong){
			msg.arg1=1;
		}else{
			msg.arg1=0;
		}
		handler.sendMessage(msg);
	}
}
