package com.funny.translation.js;

import android.util.Log;

import com.funny.translation.BaseActivity;
import com.funny.translation.MainActivity;
import com.funny.translation.MyActivityManager;

import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSStaticFunction;

public class FunnyAPI extends ScriptableObject {
    static String TAG = "FunnyAPI";

    @JSStaticFunction
    public static BaseActivity getActivity(){
        return (BaseActivity) MyActivityManager.getInstance().getCurrentActivity();
    }

    @JSStaticFunction
    public static void printSelf(){
        Log.i(TAG,"我是FunnyAPI");
    }

    @Override
    public String getClassName() {
        return TAG;
    }
}
