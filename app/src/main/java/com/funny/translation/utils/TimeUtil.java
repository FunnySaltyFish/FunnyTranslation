package com.funny.translation.utils;

import android.util.Log;

public class TimeUtil {
    private static long mTime;
    private static String TAG = "TimeUtil";
    public static void start(){
        mTime = System.currentTimeMillis();
        Log.i(TAG,"代码开始计时……");
    }
    public static void end(){
        Log.i(TAG,"计时结束，用时"+(System.currentTimeMillis()-mTime)+"ms");
    }
}
