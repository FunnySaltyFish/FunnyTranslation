package com.funny.translation;
import android.app.Application;
import com.danikula.videocache.HttpProxyCacheServer;
import android.content.Context;
import com.danikula.videocache.file.FileNameGenerator;
import com.funny.translation.utils.FileUtil;
import com.funny.translation.utils.StringUtil;
//import com.qw.soul.permission.SoulPermission;

public class FunnyApplication extends Application
{
	private HttpProxyCacheServer proxy;

	public static Context context;
	public FunnyApplication(){
		super();

	}
    public static HttpProxyCacheServer getProxy(Context context) {
        FunnyApplication myApplication = (FunnyApplication) context.getApplicationContext();
        return myApplication.proxy == null ? (myApplication.proxy = myApplication.newProxy()) : myApplication.proxy;
    }

	public static Context getFunnyContext() {
		return context;
	}

    private HttpProxyCacheServer newProxy() {
        HttpProxyCacheServer server= new HttpProxyCacheServer.Builder(this)
			.cacheDirectory(FileUtil.getAudioCacheDir(this))
			.fileNameGenerator(new FunnyFileNameGenerator())
			.maxCacheSize(10*1024*1024)
			.build()
			;
		return server;
    }

    public class FunnyFileNameGenerator implements FileNameGenerator {//缓存的命名规则
        public String generate(String url) {
			String md5="";
            try
			{
				md5=StringUtil.md5(url);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			//System.out.println("md5:"+md5);
            return md5 + ".mp3";
        }
    }
	
	
	
	@Override
	public void onCreate()
	{
		// TODO: Implement this method
		super.onCreate();
		context=getApplicationContext();
		//SoulPermission.init(this);
	}
	
	
}
