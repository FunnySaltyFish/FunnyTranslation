package com.funny.translation.translation;

import android.util.Log;

import com.funny.translation.bean.Consts;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class TranslationYouDaoEasy extends BasicTranslationTask {

    private static final String TAG = "TranslationYouDaoEasy";

    public TranslationYouDaoEasy(TranslationHelper helper, String sourceString, short sourceLanguage, short targetLanguage, short engineKind) {
        super(helper, sourceString, sourceLanguage, targetLanguage, engineKind);
    }

    @Override
    String getBasicText(String url) throws TranslationException {
        /*最简单的有道翻译接口，翻译结果差 最早写出来的*/
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
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
            String from= Consts.LANGUAGES[sourceLanguage][engineKind];
            String to=Consts.LANGUAGES[targetLanguage][engineKind];
            Log.i(TAG,"获取到的to"+to);
            String param="doctype=json&type="+from+"2"+to+"&i="+ URLEncoder.encode(sourceString,"UTF-8");
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
            throw new TranslationException(Consts.ERROR_POST);
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
                throw new TranslationException(Consts.ERROR_IO);
            }
        }
        Log.i(TAG,"获取到basicText:"+result);
        return result;
    }

    @Override
    TranslationResult getFormattedResult(String basicText) throws TranslationException {
        TranslationResult result = new TranslationResult(engineKind);
        try
        {
            StringBuilder sb=new StringBuilder();
            JSONObject all=new JSONObject(basicText);
            if(all.has("errorCode")&&all.getInt("errorCode")>0){//出错
                switch(all.getInt("errorCode")){
                    case 40:
                        throw new TranslationException(Consts.ERROR_UNSUPPORT_LANGUAGE);
                    case 50:
                        throw new TranslationException(Consts.ERROR_DATED_ENGINE);
                    default:
                        throw new TranslationException(Consts.ERROR_UNKNOWN);
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
            result.setBasicResult(sb.toString());
            return result;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            throw new TranslationException(Consts.ERROR_JSON);
        }
        catch(Exception e){
            e.printStackTrace();
            throw new TranslationException(Consts.ERROR_UNKNOWN);
        }
    }

    @Override
    String madeURL() {
        return "http://fanyi.youdao.com/translate";
    }

    @Override
    boolean isOffline() {
        return false;
    }
}
