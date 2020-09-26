package com.funny.translation.utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

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

	public static String getAssetsData(Context context, String fileName) throws IOException {
		//将json数据变成字符串
		StringBuilder stringBuilder = new StringBuilder();
		try {
			//获取assets资源管理器
			AssetManager assetManager = context.getAssets();
			//通过管理器打开文件并读取
			BufferedReader bf = new BufferedReader(new InputStreamReader(
					assetManager.open(fileName)));
			String line;
			while ((line = bf.readLine()) != null) {
				stringBuilder.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stringBuilder.toString();
	}
}
