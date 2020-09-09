package com.funny.translation.bean;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.funny.translation.ErrorDialogActivity;
import com.funny.translation.FunnyApplication;

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

//            final String crashReason = getCrashReport(ex);
//            AlertDialog dialog = new AlertDialog.Builder(applicationContext)
//                    .setTitle("抱歉，应用程序发生了崩溃！")
//                    .setMessage("应用程序发生了崩溃，我们建议您发送崩溃报告以反馈。\n\n为什么需要崩溃报告？" +
//                            "\n应用崩溃对于使用者和开发者来说都是相当大的灾难，我们都希望将其修复。但是，没有报错原因的崩溃，犹如坏了的无法打开的黑盒子，" +
//                            "你只知道出现问题，却不知道是什么问题、问题在哪，难以完成修复。因此，还望您可以提供病症，我们才可以对症下药。" +
//                            "\n\n具体原因如下:\n" + crashReason)
//                    .setPositiveButton("复制并反馈", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            ClipBoardUtil.copy(applicationContext,crashReason);
//                            ApplicationUtil.startWebBrowse(applicationContext,"https://support.qq.com/embed/phone/191372");
//                            destroy();
//                        }
//                    })
//                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            destroy();
//                        }
//                    })
//                    .create();
//            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private String getCrashReport(Throwable ex) throws PackageManager.NameNotFoundException {
        StringBuilder exceptionStr = new StringBuilder();
        PackageInfo pinfo = FunnyApplication.getInstance().getLocalPackageInfo();
        if (pinfo != null) {
            if (ex != null) {
                //app版本信息
                exceptionStr.append("App Version：").append(pinfo.versionName);
                exceptionStr.append("_").append(pinfo.versionCode).append("\n");

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
                if (elements != null) {
                    for (StackTraceElement element : elements) {
                        exceptionStr.append(element.toString()).append("\n");
                    }
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
