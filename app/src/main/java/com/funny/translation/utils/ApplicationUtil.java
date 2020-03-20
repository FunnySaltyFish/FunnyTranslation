package com.funny.translation.utils;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.widget.Toast;
import android.view.LayoutInflater;
import com.funny.translation.R;
import android.view.View;
import android.widget.TextView;
import java.io.InputStream;
import java.io.IOException;
import android.content.SharedPreferences;
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
			toast.setDuration(Toast.LENGTH_SHORT);
		}
		toast.show();
	}
	
	public static void print(Context ctx,String str,boolean isLong){
		if(toast==null){//单例模式，加快展示速度
			toast=new Toast(ctx);
			View v=LayoutInflater.from(ctx).inflate(R.layout.view_toast,null);
			toast.setDuration(Toast.LENGTH_SHORT);
			toastTV=v.findViewById(R.id.view_toast_tv);
			toastTV.setText(str);
			toast.setView(v);
		}else{
			toastTV.setText(str);
			toast.setDuration(isLong?Toast.LENGTH_LONG:Toast.LENGTH_SHORT);
		}
		toast.show();
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
	
	public static int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

}
