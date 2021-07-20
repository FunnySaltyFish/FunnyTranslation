package com.funny.translation.translation;

import com.funny.translation.bean.Consts;
import com.funny.translation.utils.StringUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TranslationBaiduNormal extends BasicTranslationTask {
    String TAG="BaiduTranslation";

    public TranslationBaiduNormal(String sourceString, short sourceLanguage, short targetLanguage) {
        super(sourceString, sourceLanguage, targetLanguage);
    }


    @Override
    public String getBasicText(String url) throws TranslationException {
        //Log.i(TAG,String.format("正在使用百度翻译！用的appid是%s",Consts.BAIDU_APP_ID));
        short engineKind = getEngineKind();
        BaiduTransApi api=BaiduTransApi.getBaiduTransApi(Consts.BAIDU_APP_ID,Consts.BAIDU_SECURITY_KEY);
        String from=Consts.LANGUAGES[sourceLanguage][engineKind];
        String to= Consts.LANGUAGES[targetLanguage][engineKind];
        try {
            String transResult = api.getTransResult(sourceString, from, to);
            //Log.i(TAG,"baidu api获取到的基本result是"+transResult);
            return transResult;
        }catch (TranslationException e){
            throw e;
        }
}

    @Override
    public TranslationResult getFormattedResult(String basicText) throws TranslationException {
        TranslationResult translationResult=new TranslationResult(Consts.ENGINE_BAIDU_NORMAL);
        try
        {
            StringBuilder sb=new StringBuilder();
            JSONObject all=new JSONObject(basicText);
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
            translationResult.setBasicResult(sb.toString());
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
        return translationResult;
    }

    @Override
    public String madeURL() {
        return null;
    }

    @Override
    public boolean isOffline() {
        return false;
    }

    @Override
    public short getEngineKind() {
        return Consts.ENGINE_BAIDU_NORMAL;
    }
}
