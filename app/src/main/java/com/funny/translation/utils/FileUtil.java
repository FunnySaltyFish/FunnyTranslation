package com.funny.translation.utils;
import java.io.File;
import android.content.Context;

public class FileUtil
{
	/**
     * 音乐播放缓存目录的设置
     * @param context
     * @return
     */
    public static File getAudioCacheDir(Context context) {
        return new File(context.getCacheDir(), "audio-cache");
    }
	
	public static boolean isCached(Context ctx,String url){
		File cacheFile=getAudioCacheDir(ctx);
		File[] allFiles=cacheFile.listFiles();
		try
		{
			String fileName=StringUtil.md5(url);
			for (File f:allFiles)
			{
				//System.out.println(f.getName().substring(0,f.getName().lastIndexOf(".")));
				if(f.getName().substring(0,f.getName().lastIndexOf(".")).equals(fileName)){
					return true;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
}
