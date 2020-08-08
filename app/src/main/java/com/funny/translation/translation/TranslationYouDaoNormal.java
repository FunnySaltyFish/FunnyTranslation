package com.funny.translation.translation;

import com.funny.translation.bean.Consts;
import com.funny.translation.utils.StringUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class TranslationYouDaoNormal extends BasicTranslationTask {
    public TranslationYouDaoNormal(TranslationHelper helper, String sourceString, short sourceLanguage, short targetLanguage, short engineKind) {
        super(helper, sourceString, sourceLanguage, targetLanguage, engineKind);
    }

    @Override
    String getBasicText(String url) throws TranslationException {
        OutputStreamWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
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
            String from= Consts.LANGUAGES[sourceLanguage][engineKind];
            String to=Consts.LANGUAGES[targetLanguage][engineKind];
            long salt=System.currentTimeMillis()+(long)(Math.random()*9+1);
            String bv= StringUtil.md5("5.0 (Windows)");
            //System.out.println(bv);
            String sign =StringUtil.md5("fanyideskweb"+sourceString+salt+"Nw(nmmbP%A-r6U3EUn]Aj");
            //"mmbP%A-r6U3Nw(n]BjuEU"); 2020/7/15失败
            ///*"n%A-rKaT5fb[Gy?;N5@Tj"*/);
            //System.out.println("fanyideskweb"+sourceString+salt+"n%A-rKaT5fb[Gy?;N5@Tj");
            String param="doctype=json&from="+from+"&to="+to+"&i="+ URLEncoder.encode(sourceString,"UTF-8")+"&client=fanyideskweb&smartresult=dict"+
                    "&salt="+salt+
                    "&sign="+sign+
                    "&ts="+salt+
                    "&bv="+bv+
                    "&version=2.1&keyfrom=fanyi.web&action=FY_BY_CLICKBUTTION"
                    ;
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
        return result;
    }

    @Override
    TranslationResult getFormattedResult(String basicText) throws TranslationException {
        TranslationResult result = new TranslationResult(engineKind);
        try {
            StringBuilder sb = new StringBuilder();
            JSONObject all = new JSONObject(basicText);
            if (all.has("errorCode") && all.getInt("errorCode") > 0) {//出错
                switch (all.getInt("errorCode")) {
                    case 40:
                        throw new TranslationException(Consts.ERROR_UNSUPPORT_LANGUAGE);
                    case 50:
                        throw new TranslationException(Consts.ERROR_DATED_ENGINE);
                    default:
                        throw new TranslationException(Consts.ERROR_UNKNOWN);
                }
            }
            JSONArray translationResult = all.getJSONArray("translateResult");
            for (int i = 0; i < translationResult.length(); i++) {
                JSONArray eachResult = translationResult.getJSONArray(i);
                for (int j = 0; j < eachResult.length(); j++) {
                    JSONObject resultObject = eachResult.getJSONObject(j);
                    String resultString = resultObject.getString("tgt");
                    sb.append(resultString);
                    sb.append("\n");
                }
            }
            sb.deleteCharAt(sb.length() - 1);
            result.setBasicResult(sb.toString());
            return result;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            throw new TranslationException(Consts.ERROR_JSON);
        }
        catch (TranslationException e){
            e.printStackTrace();
            throw e;
        }
        catch(Exception e){
            e.printStackTrace();
            throw new TranslationException(Consts.ERROR_UNKNOWN);
        }
    }

    @Override
    String madeURL() {
        return "http://fanyi.youdao.com/translate_o?smartresult=dict&smartresult=rule";
    }

    @Override
    boolean isOffline() {
        return false;
    }
}
