package com.funny.translation.translation;
import com.funny.translation.bean.Consts;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.net.URL;
import java.net.URLConnection;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URLEncoder;

import static com.funny.translation.bean.Consts.*;
import com.funny.translation.utils.StringUtil;
import java.io.OutputStreamWriter;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
public class TranslationTask
{
	public short engineKind;//调用的基本web接口
	public String sourceString;
	public String resultString;

	public short sourceLanguage;
	public short targetLanguage;
	
	private int id;
	private OnTranslateListener listener;

	public TranslationTask(short engineKind,short sourceLanguage, short targetLanguage, String sourceString)
	{
		this.engineKind = engineKind;
		this.sourceString = sourceString;
		this.sourceLanguage = sourceLanguage;
		this.targetLanguage = targetLanguage;
	}
	public TranslationTask(short engineKind,String sourceString)
	{
		this.engineKind=engineKind;
		this.sourceString = sourceString;
		this.sourceLanguage=LANGUAGE_AUTO;
		this.targetLanguage=LANGUAGE_AUTO;
	}

	public void translate(){
		switch(engineKind){
			case Consts.ENGINE_YOUDAO_EASY:
				this.resultString=translateYouDaoEasy(sourceString);
				break;
			case Consts.ENGINE_YOUDAO_NORMAL:
				this.resultString=translateYouDaoNormal(sourceString);
				break;
			case Consts.ENGINE_BAIDU_NORMAL:
				this.resultString=translateBaiduNormal(sourceString);
				break;
		}
		//System.out.println(resultString);
	}

	public String formatResultYoudao(String str){
		try
		{
			StringBuilder sb=new StringBuilder();
			JSONObject all=new JSONObject(str);
			if(all.has("errorCode")){//出错
				switch(all.getInt("errorCode")){
					case 40:
						return Consts.ERROR_UNSUPPORT_LANGUAGE;
					case 50:
						return Consts.ERROR_DATED_ENGINE;
				}
			}
			JSONArray translationResult=all.getJSONArray("translateResult");
			for (int i=0;i < translationResult.length();i++)
			{
				JSONArray eachResult=translationResult.getJSONArray(i);
				for (int j=0;j<eachResult.length();j++)
				{
					JSONObject resultObject=eachResult.getJSONObject(j);
					String resultString=resultObject.getString("tgt");
					sb.append(resultString);
					sb.append("\n");
				}
			}
			sb.deleteCharAt(sb.length()-1);
			listener.onSuccess(this.sourceString,sb.toString());
			return (sb.toString());
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			listener.onFail(Consts.ERROR_JSON);
			return Consts.ERROR_JSON;
		}
		catch(Exception e){
			listener.onFail(Consts.ERROR_UNKNOWN);
			return Consts.ERROR_UNKNOWN;
		}
	}

	public String translateYouDaoEasy(String sourceString){
		/*最简单的有道翻译接口，翻译结果差 最早写出来的*/
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			URL realUrl = new URL("http://fanyi.youdao.com/translate");
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属性
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			//1.获取URLConnection对象对应的输出流
			out = new PrintWriter(conn.getOutputStream());
			String from=Consts.LANGUAGES[sourceLanguage][engineKind];
			String to=Consts.LANGUAGES[targetLanguage][engineKind];
			String param="doctype=json&type="+from+"2"+to+"&i="+URLEncoder.encode(sourceString,"UTF-8");
			out.print(param);
			// flush输出流的缓冲
			out.flush();
			// 定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			System.out.println("发送 POST 请求出现异常！"+e);
			e.printStackTrace();
			result=("发送 POST 请求出现异常！"+e);
		}
		//使用finally块来关闭输出流、输入流
		finally{
			try{
				if(out!=null){
					out.close();
				}
				if(in!=null){
					in.close();
				}
			}
			catch(IOException ex){
				ex.printStackTrace();
				result=ex.getMessage();
			}
		}
		result=formatResultYoudao(result);
		//System.out.println("post推送结果："+result);
		return result;
	}
	
	public String translateYouDaoNormal(String sourceString){
		/*
			较为精准的有道翻译，在查阅各种乱七八糟的资料后由python版本修改而来
			一个坑是数据返回时的特殊处理
		*/
		OutputStreamWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			URL realUrl = new URL("http://fanyi.youdao.com/translate_o?smartresult=dict&smartresult=rule");
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属性
			conn.setRequestProperty("Host","fanyi.youdao.com");
			conn.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:69.0) Gecko/20100101 Firefox/69.0");
			conn.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
			conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
			//conn.setRequestProperty("Accept-Encoding","gzip, deflate");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			conn.setRequestProperty("X-Requested-With","XMLHttpRequest");
			//conn.setRequestProperty("Content-Length","262");
			conn.setRequestProperty("Connection","keep-alive");
			conn.setRequestProperty("Referer","http://fanyi.youdao.com/");
			conn.setRequestProperty("Cookie","YOUDAO_MOBILE_ACCESS_TYPE=1; OUTFOX_SEARCH_USER_ID=1378414660@10.108.160.19; JSESSIONID=aaaBTGxpwV4EQfnO_Oy1w; ___rl__test__cookies=1569154996426; OUTFOX_SEARCH_USER_ID_NCOO=752434577.0207007");
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			//防止中文乱码
			out=new OutputStreamWriter(conn.getOutputStream(),"UTF-8");
			// 发送请求参数
			String from=Consts.LANGUAGES[sourceLanguage][engineKind];
			String to=Consts.LANGUAGES[targetLanguage][engineKind];
			long salt=System.currentTimeMillis()+(long)(Math.random()*9+1);
			String bv=StringUtil.md5("5.0 (Windows)");
			//System.out.println(bv);
			String sign =StringUtil.md5("fanyideskweb"+sourceString+salt+"n%A-rKaT5fb[Gy?;N5@Tj");
			//System.out.println("fanyideskweb"+sourceString+salt+"n%A-rKaT5fb[Gy?;N5@Tj");
			//System.out.println(sign);
			String param="doctype=json&from="+from+"&to="+to+"&i="+URLEncoder.encode(sourceString,"UTF-8")+"&client=fanyideskweb&smartresult=dict"+
				"&salt="+salt+
				"&sign="+sign+
				"&ts="+salt+
				"&bv="+bv+
				"&version=2.1&keyfrom=fanyi.web&action=FY_BY_CLICKBUTTION"
				;
			//out.print(param);
			out.write(param,0,param.length());
			// flush输出流的缓冲
			out.flush();
			// 定义BufferedReader输入流来读取URL的响应
			//返回数据为gzip类型，解压缩
			InputStream stream = conn.getInputStream();
			in = new BufferedReader(new InputStreamReader(stream,"utf-8"));  
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
			//System.out.println("翻译后直接获得的result"+result);
			//result=new String(result.getBytes("utf-8"),"UTF-8");
			result=formatResultYoudao(result);
		} catch (Exception e) {
			System.out.println("发送 POST 请求出现异常！"+e);
			e.printStackTrace();
			result=("发送 POST 请求出现异常！");
		}
		//使用finally块来关闭输出流、输入流
		finally{
			try{
				if(out!=null){
					out.close();
				}
				if(in!=null){
					in.close();
				}
			}
			catch(IOException ex){
				ex.printStackTrace();
				result=ex.getMessage();
			}
		}
		return result;
	}
	
	public String translateBaiduNormal(String sourceString){
		/*基于百度官方代码示例，最为稳定！*/
		BaiduTransApi api=new BaiduTransApi(Consts.BAIDU_APP_ID,Consts.BAIDU_SECURITY_KEY);
		String from=Consts.LANGUAGES[sourceLanguage][engineKind];
		String to=Consts.LANGUAGES[targetLanguage][engineKind];
		String result=api.getTransResult(sourceString,from,to);
		//System.out.println("翻译后直接获得的result"+result);
		return formatResultBaidu(result);
	}
	
	public String formatResultBaidu(String str){
		try
		{
			StringBuilder sb=new StringBuilder();
			JSONObject all=new JSONObject(str);
			JSONArray trans_result=all.getJSONArray("trans_result");
			for(int i=0;i<trans_result.length();i++){
				JSONObject resultObj=trans_result.getJSONObject(i);
				String str1=resultObj.getString("dst");
				if(StringUtil.isUnicode(str1)){
					str1=StringUtil.unicodeToString(str1);
				}
				sb.append(str1);
				sb.append("\n");
			}
			sb.deleteCharAt(sb.length()-1);
			listener.onSuccess(this.sourceString,sb.toString());
			return sb.toString();
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			listener.onFail(Consts.ERROR_JSON);
			return Consts.ERROR_JSON;
		}
		catch(Exception e){
			listener.onFail(Consts.ERROR_UNKNOWN);
			return Consts.ERROR_UNKNOWN;
		}
	}

	public void translateGoogleEasy(){
		//"http://translate.google.cn/translate_a/single?client=gtx&dt=t&dj=1&ie=UTF-8&sl=auto&tl=zh_TW&q=calculate";
		/*复杂版本
		 translate.google.cn/translate_a/single?client=webapp&sl=en&tl=zh-CN&hl=zh-CN&dt=at&dt=bd&dt=ex&dt=ld&dt=md&dt=qca&dt=rw&dt=rm&dt=ss&dt=t&source=btn&ssel=5&tsel=5&kc=0&tk=510384.97655&q=hello
		*/
		/*多行
		translate.google.cn/translate_a/single?client=webapp&sl=en&tl=zh-CN&hl=zh-CN&dt=at&dt=bd&dt=ex&dt=ld&dt=md&dt=qca&dt=rw&dt=rm&dt=ss&dt=t&ssel=5&tsel=5&kc=1&tk=936052.588979&q=public%20static%20String%20replacePunctuation%20(String%20sourceString)%20%7B%0APattern%20p%20%3D%20Pattern.compile%20(%22%5B.%3F%3A%2C%22%20%22!%20%3C%3E%5D%22)%3B%0AMatcher%20m%20%3D%20p.matcher%20(sourceString)%3B%0AString%20result%20%3D%20null%3B%0Aif%20(m.find%20())%20%7B%0Aresult%20%3D%20m.replaceFirst%20(%22%22)%3B%0A%7D%0Areturn%20result%20%3D%3D%20null%3F%20sourceString%3A%20result%3B%0A%7D
		*/
		/*百度tts
		https://fanyi.baidu.com/gettts?lan=fra&text=Ha%20ha%20ha%20ha%20ha%20ha&spd=3&source=wise
		*/
		/*谷歌tts
		https://translate.google.cn/translate_tts?ie=UTF-8&q=Hello%20there%20Azure%20is%20waiting%20for%20the%20smoke%20and%20rain%2C%20and%20I%20am%20waiting%20for%20you.&tl=en&total=1&idx=0&textlen=78&tk=91195.512248&client=webapp
		*/
	}
	
	public void setLisener(OnTranslateListener lisener)
	{
		this.listener = lisener;
	}

	public OnTranslateListener getLisener()
	{
		return listener;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public int getId()
	{
		return id;
	}

}
