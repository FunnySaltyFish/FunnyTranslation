package com.funny.translation;
import android.app.Activity;
import android.app.Application;
import com.danikula.videocache.HttpProxyCacheServer;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.danikula.videocache.file.FileNameGenerator;
import com.funny.translation.bean.FunnyUncaughtExceptionHandler;
import com.funny.translation.utils.FileUtil;
import com.funny.translation.utils.OkHttpUtil;
import com.funny.translation.utils.StringUtil;
import com.hjq.toast.ToastUtils;
//import com.qw.soul.permission.SoulPermission;

public class FunnyApplication extends Application
{
	private HttpProxyCacheServer proxy;
	static FunnyApplication funnyApplication;
	public static Context context;

	@Override
	public void onCreate()
	{
		// TODO: Implement this method
		super.onCreate();
		context=getApplicationContext();
		funnyApplication = this;
		FunnyUncaughtExceptionHandler funnyUncaughtExceptionHandler = FunnyUncaughtExceptionHandler.getInstance();
		funnyUncaughtExceptionHandler.init(getFunnyContext());
		OkHttpUtil.init();
		ToastUtils.init(this);
		initActivityManager();
//		EasyHttp.init(this);
//		EasyHttp.getInstance().debug("EasyHttp",true);
		//SoulPermission.init(this);
	}

	private void initActivityManager() {
			registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
				@Override
				public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
					MyActivityManager.getInstance().setCurrentActivity(activity);
				}

				@Override
				public void onActivityStarted(Activity activity) {
				}

				@Override
				public void onActivityResumed(Activity activity) {
					MyActivityManager.getInstance().setCurrentActivity(activity);
				}

				@Override
				public void onActivityPaused(Activity activity) {
				}

				@Override
				public void onActivityStopped(Activity activity) {
				}

				@Override
				public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
				}

				@Override
				public void onActivityDestroyed(Activity activity) {
				}
			});

	}

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

	public static FunnyApplication getInstance(){
		return funnyApplication;
	}

	public PackageInfo getLocalPackageInfo() throws PackageManager.NameNotFoundException {
		return context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
	}
	
}
