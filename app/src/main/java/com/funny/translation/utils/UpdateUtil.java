package com.funny.translation.utils;
import org.json.JSONObject;
import org.json.JSONException;
import android.content.Context;
import java.net.URL;
import java.net.MalformedURLException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

public class UpdateUtil
{
	public static JSONObject updateDescription;
	public static String updateLog;
	public static boolean isUpdate=true;
	public static JSONObject getUpdateDescription() throws Exception{
		JSONObject obj=null;
		try
		{
			String str = OkHttpUtil.get("https://gitee.com/funnysaltyfish/FunnyTranslationDownload/raw/master/description.json");
			obj= new JSONObject(str);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			throw new JSONException("自动更新失败！JSON解析错误！");
		}
		catch (Exception e){
			e.printStackTrace();
			throw e;
		}
		return obj;
	}
	
	public static boolean checkNewVersion(Context ctx){
		if(updateDescription==null){
			return false;
		}
		int curVersionCode=ApplicationUtil.getVersionCode(ctx);
		try
		{
			//System.out.println("updateDes:"+updateDescription);
			int newVersionCode=updateDescription.getInt("versionCode");
			System.out.printf("newVersion:%d    curVersion:%d",newVersionCode,curVersionCode);
			if(newVersionCode>curVersionCode){
				isUpdate=getIsUpdate();
				return true;
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			return false;
		}
		return false;
	}
	
	public static String getUpdateLog(){
		if(updateDescription==null)return null;
		try
		{
			updateLog = updateDescription.getString("updateLog");
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return updateLog;
	}
	
	public static String getApkUrl(){
		if(updateDescription==null)return null;
		String url=null;
		try
		{
			url=updateDescription.getString("apkUrl");
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			return null;
		}
		return url;
	}
	
	public static boolean getIsUpdate(){
		if(updateDescription==null)return true;
		boolean result=true;
		try
		{
			if(updateDescription.has("isUpdate"))
				result=updateDescription.getBoolean("isUpdate");
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		System.out.println("IsUpdate:"+result);
		return result;
	}
	
	public static void startUpdateByBrowse(Context ctx,String url){
		Intent intent=new Intent();
		Uri uri=Uri.parse(url);
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(uri);
		ctx.startActivity(intent);
	}
}
