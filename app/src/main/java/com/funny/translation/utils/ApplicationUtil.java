package com.funny.translation.utils;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.widget.Toast;
import android.view.LayoutInflater;

import com.funny.translation.FunnyApplication;
import com.funny.translation.R;
import com.hjq.toast.ToastUtils;

import android.view.View;
import android.widget.TextView;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import android.content.SharedPreferences;
public class ApplicationUtil
{
//	public static int MAX_TOASTS = 3;
//	public static Toast[] toasts=null;
//	public static TextView[] toastTVs=null;
//	private static int curToast = 0;
	//private static TextView toastTV;
	private static boolean hasInitToast = false;
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
//		if (toasts==null){//完全初始化
//			toasts = new Toast[MAX_TOASTS];
//			toastTVs = new TextView[MAX_TOASTS];
//
//			for (int i = 0; i < MAX_TOASTS; i++) {
//				Toast toast = new Toast(ctx);
//				View v=LayoutInflater.from(ctx).inflate(R.layout.view_toast,null);
//				TextView toastTV=v.findViewById(R.id.view_toast_tv);
//				toast.setView(toastTV);
//				toasts[i] = toast;
//				toastTVs[i] = toastTV;
//			}
//		}
//
//		toasts[curToast].setDuration(Toast.LENGTH_SHORT);
//		toastTVs[curToast].setText(str);
//		toasts[curToast].show();
//
//		curToast++;
//		if (curToast==MAX_TOASTS)curToast=0;
		print(ctx, str,false);
	}

	
	public static void print(Context ctx,String str,boolean isLong){
		if (!hasInitToast){
			ToastUtils.setView(R.layout.view_toast);
			ToastUtils.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL,0,200);
			hasInitToast = true;
		}
		ToastUtils.show(str);
	}

	public static void print(String str){
		if (!hasInitToast){
			ToastUtils.setView(R.layout.view_toast);
			ToastUtils.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL,0,200);
			hasInitToast = true;
		}
		ToastUtils.show(str);
	}
	
	public static String getTextFromAssets(Context ctx,String fileName){
		InputStream is=null;
		String msg="";
		try
		{
			is = ctx.getAssets().open(fileName);
			byte[] bytes=new byte[is.available()];
			is.read(bytes);
			msg=new String(bytes);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return "";
		}finally{
			if(is!=null){
				try
				{
					is.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		return msg;
	}
	
	public static int getVersionCode(Context ctx){
		int versionCode=0;
		try{
			versionCode=ctx.getPackageManager().getPackageInfo(ctx.getPackageName(),0).versionCode;
		}catch(Exception e){
			e.printStackTrace();
		}
		return versionCode;
	}
	
	public static boolean isFirstOpen(Context ctx){//是否是第一次打开应用
		int newVersionCode=getVersionCode(ctx);
		SharedPreferences sp=ctx.getSharedPreferences("welcomeInfo",Context.MODE_PRIVATE);
		int oldVersionCode=sp.getInt("versionCode",0);
		if(newVersionCode>oldVersionCode){
			SharedPreferences.Editor editor=sp.edit();
			editor.putInt("versionCode",newVersionCode);
			editor.commit();
			return true;
		}else{
			return false;
		}
	}

	/*获取错误信息*/
	public static String getErrorMessage(Throwable t){
		StringWriter stringWriter=new StringWriter();
		t.printStackTrace(new PrintWriter(stringWriter,true));
		return stringWriter.getBuffer().toString();
	}

	public static void startWebBrowse(Context ctx, String url) {
		Intent intent = new Intent();
		Uri uri = Uri.parse(url);
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(uri);
		ctx.startActivity(intent);
	}
	
	public static int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

}
