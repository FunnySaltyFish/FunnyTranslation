package com.funny.translation;
import android.app.Application;
import com.danikula.videocache.HttpProxyCacheServer;
import android.content.Context;
import com.danikula.videocache.file.FileNameGenerator;
import android.net.Uri;
import com.funny.translation.utils.FileUtils;
import com.funny.translation.translation.MD5;
import com.danikula.videocache.CacheListener;
import java.io.File;
//import com.qw.soul.permission.SoulPermission;

public class FunnyApplication extends Application
{
	private HttpProxyCacheServer proxy;

    public static HttpProxyCacheServer getProxy(Context context) {
        FunnyApplication myApplication = (FunnyApplication) context.getApplicationContext();
        return myApplication.proxy == null ? (myApplication.proxy = myApplication.newProxy()) : myApplication.proxy;
    }

    private HttpProxyCacheServer newProxy() {
        HttpProxyCacheServer server= new HttpProxyCacheServer.Builder(this)
			.cacheDirectory(FileUtils.getAudioCacheDir(this))
			.fileNameGenerator(new FunnyFileNameGenerator())
			.maxCacheSize(10*1024*1024)
			.build()
			;
		return server;
    }

    public class FunnyFileNameGenerator implements FileNameGenerator {//缓存的命名规则
        public String generate(String url) {
            //Uri uri = Uri.parse(url);
            //String audioId = uri.getQueryParameter("guid");
			//System.out.println("id:"+audioId);
			//long audioId=System.currentTimeMillis();
			String md5=MD5.md5(url);
			//System.out.println("md5:"+md5);
            return md5 + ".mp3";
        }
    }
	
	
	
	@Override
	public void onCreate()
	{
		// TODO: Implement this method
		super.onCreate();
		//SoulPermission.init(this);
	}
	
	
}
