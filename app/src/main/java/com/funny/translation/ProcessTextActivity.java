package com.funny.translation;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

import java.util.Objects;

public class ProcessTextActivity extends BaseActivity {
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        ((WindowManager.LayoutParams) layoutParams).flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        //ApplicationUtil.print(this,getCheckedText(getIntent()));

        Intent intent = new Intent();
        intent.setClass(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        Intent gotIntent = getIntent();
        if (Objects.equals(gotIntent.getAction(), Intent.ACTION_SEND)){//分享过来的
            intent.putExtra("shared_text",getSharedText(gotIntent));
        }else if (Objects.equals(gotIntent.getAction(), Intent.ACTION_PROCESS_TEXT)){//文本选择菜单来的
            intent.putExtra("checked_text",getCheckedText(gotIntent));
        }

        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private String getCheckedText(Intent intent){
        CharSequence text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
        assert text != null;
        return text.toString();
    }

    private String getSharedText(Intent intent){
        CharSequence text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
        assert text != null;
        return text.toString();
    }
}
