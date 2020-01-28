package com.funny.translation.utils;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.widget.Toast;
import android.view.LayoutInflater;
import com.funny.translation.R;
import android.view.View;
import android.widget.TextView;
public class ApplicationUtil
{
	public static Toast toast=null;
	public static TextView toastTV=null;
	public static void copyToClipboard(Context ctx,String content){
		//获取剪贴板管理器：
		// 获取系统剪贴板
        ClipboardManager clipboard = (ClipboardManager)ctx.getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建一个剪贴数据集，包含一个普通文本数据条目（需要复制的数据）
        ClipData clipData = ClipData.newPlainText(null,content);
        // 把数据集设置（复制）到剪贴板
        clipboard.setPrimaryClip(clipData);
		
		/*
		来自：https://www.jianshu.com/p/1e84d33154bd
		*/
	}
	
	public static void print(Context ctx,String str){
		if(toast==null){//单例模式，加快展示速度
			toast=new Toast(ctx);
			View v=LayoutInflater.from(ctx).inflate(R.layout.view_toast,null);
			toast.setDuration(Toast.LENGTH_SHORT);
			toastTV=v.findViewById(R.id.view_toast_tv);
			toastTV.setText(str);
			toast.setView(v);
		}else{
			toastTV.setText(str);
		}
		toast.show();
	}
}
