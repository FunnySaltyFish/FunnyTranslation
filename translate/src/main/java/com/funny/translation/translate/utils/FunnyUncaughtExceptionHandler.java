package com.funny.translation.translate.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.funny.translation.BaseApplication;
import com.funny.translation.translate.ErrorDialogActivity;
import com.funny.translation.translate.FunnyApplication;


public class FunnyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler{
    Context applicationContext;
    boolean crashing;
    Thread.UncaughtExceptionHandler mDefaultHandler;

    static FunnyUncaughtExceptionHandler mHandler;

    public void init(Context ctx){
        applicationContext = ctx.getApplicationContext();
        crashing = false;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public static FunnyUncaughtExceptionHandler getInstance(){
        return (mHandler==null?mHandler=new FunnyUncaughtExceptionHandler():mHandler);
    }

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
        if (crashing)return;
        crashing = true;
        throwable.printStackTrace();
        if (!handleCrashByMe(throwable) && mDefaultHandler != null) {
            // 系统处理
            mDefaultHandler.uncaughtException(thread, throwable);
        }
        destroy();
    }

    private boolean handleCrashByMe(Throwable ex){
        if (ex==null)return false;
        try{
            System.out.println("接管了应用的报错！");
            Intent intent = new Intent();
            intent.setClass(applicationContext, ErrorDialogActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            intent.putExtra("CRASH_MESSAGE", getCrashReport(ex));
            applicationContext.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private String getCrashReport(Throwable ex) throws PackageManager.NameNotFoundException {
        StringBuilder exceptionStr = new StringBuilder();
        PackageInfo packageInfo = BaseApplication.Companion.getLocalPackageInfo();
        if (packageInfo != null) {
            if (ex != null) {
                //app版本信息
                exceptionStr.append("App Version：").append(packageInfo.versionName);
                exceptionStr.append("_").append(packageInfo.versionCode).append("\n");

                //手机系统信息
                exceptionStr.append("OS Version：").append(Build.VERSION.RELEASE);
                exceptionStr.append("_");
                exceptionStr.append(Build.VERSION.SDK_INT).append("\n");

                //手机制造商
                exceptionStr.append("Vendor: ").append(Build.MANUFACTURER).append("\n");

                //手机型号
                exceptionStr.append("Model: ").append(Build.MODEL).append("\n");

                String errorStr = ex.getLocalizedMessage();
                if (TextUtils.isEmpty(errorStr)) {
                    errorStr = ex.getMessage();
                }
                if (TextUtils.isEmpty(errorStr)) {
                    errorStr = ex.toString();
                }
                exceptionStr.append("Exception: ").append(errorStr).append("\n");
                StackTraceElement[] elements = ex.getStackTrace();
                for (StackTraceElement element : elements) {
                    exceptionStr.append(element.toString()).append("\n");
                }
            } else {
                exceptionStr.append("no exception. Throwable is null\n");
            }
            return exceptionStr.toString();
        } else {
            return "";
        }
    }

    private void destroy(){
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}
