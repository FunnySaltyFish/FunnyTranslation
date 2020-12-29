package com.funny.translation;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import com.funny.translation.utils.ApplicationUtil;
import com.funny.translation.utils.ClipBoardUtil;

public class ErrorDialogActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        final String crashMessage = intent.getStringExtra("CRASH_MESSAGE");
        AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("抱歉，应用程序发生了崩溃！")
                    .setMessage("应用程序发生了崩溃，我们建议您发送崩溃报告以反馈。\n\n为什么需要崩溃报告？" +
                            "\n应用崩溃对于使用者和开发者来说都是相当大的灾难，我们都希望将其修复。但是，没有报错原因的崩溃，犹如坏了的无法打开的黑盒子，" +
                            "你只知道出现问题，却不知道是什么问题、问题在哪，难以完成修复。因此，还望您可以提供病症，我们才可以对症下药。" +
                            "\n(按下复制后，您的崩溃信息将仅保存在剪贴板中，是否需要发送取决于您。开发者很注重应用的安全性，不会强制性向您索要这些信息。）" +
                            "\n\n具体原因如下:\n" + crashMessage)
                    .setPositiveButton("复制并反馈", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ClipBoardUtil.copy(ErrorDialogActivity.this,crashMessage);
                            ApplicationUtil.startWebBrowse(ErrorDialogActivity.this,"https://support.qq.com/embed/phone/191372");
                            destroy();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            destroy();
                            Context application = FunnyApplication.getFunnyContext();
                            Intent intent = new Intent(application, MainActivity.class);
                            PendingIntent restartIntent = PendingIntent.getActivity(
                                    application.getApplicationContext(), 0, intent,
                                    PendingIntent.FLAG_CANCEL_CURRENT);
                            //退出程序
                            AlarmManager mgr = (AlarmManager)application.getSystemService(Context.ALARM_SERVICE);
                            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
                                    restartIntent); // 1秒钟后重启应用
                        }
                    })
                    .create();
            dialog.show();
    }

    @Override
    public Intent getIntent() {
        return super.getIntent();
    }

    private void destroy(){
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}
