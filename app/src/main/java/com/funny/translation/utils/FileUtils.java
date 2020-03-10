package com.funny.translation.utils;
import java.io.File;
import android.content.Context;

public class FileUtils
{
	/**
     * 音乐播放缓存目录的设置
     * @param context
     * @return
     */
    public static File getAudioCacheDir(Context context) {
        return new File(context.getCacheDir(), "audio-cache");
    }
}
