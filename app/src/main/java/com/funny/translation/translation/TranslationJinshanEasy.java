package com.funny.translation.translation;

import android.util.Log;

import com.funny.translation.bean.Consts;
import com.funny.translation.utils.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class TranslationJinshanEasy extends BasicTranslationTask {
    String TAG = "TranslationJinshanEasy";
    public TranslationJinshanEasy(TranslationHelper helper, String sourceString, short sourceLanguage, short targetLanguage, short engineKind) {
        super(helper, sourceString, sourceLanguage, targetLanguage, engineKind);
    }

    @Override
    String getBasicText(String url) throws TranslationException {
        try {
            String string = OkHttpUtil.get(url);
            Log.i(TAG,"获取到的string:"+string);
            return string;
        } catch (IOException e) {
            e.printStackTrace();
            throw new TranslationException(Consts.ERROR_POST);
        }
    }

    @Override
    TranslationResult getFormattedResult(String basicText) throws TranslationException {
        TranslationResult result = new TranslationResult(engineKind);
        try {
            JSONObject all = new JSONObject(basicText);
            if (all.getInt("status")==1){
                JSONObject content = all.getJSONObject("content");
                String trans = content.getString("out");
                result.setBasicResult(trans);
            }else if(all.getInt("status")==0){
                JSONObject content = all.getJSONObject("content");
                JSONArray detailArr = content.getJSONArray("word_mean");
                StringBuilder text = new StringBuilder();
                for (int i = 0; i < detailArr.length(); i++) {
                    text.append(detailArr.getString(i));
                    text.append("\n");
                }
                text.deleteCharAt(text.length()-1);
                result.setBasicResult(text.toString());
                //result.setPhoneticNotation();
            }
            else{
                throw new TranslationException(Consts.ERROR_DATED_ENGINE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            throw new TranslationException(Consts.ERROR_JSON);
        }
        return result;
    }

    @Override
    String madeURL() {
        String from = Consts.LANGUAGES[sourceLanguage][engineKind];
        String to = Consts.LANGUAGES[targetLanguage][engineKind];
        String url = null;
        try {
            url = String.format("http://fy.iciba.com/ajax.php?a=fy&f=%s&t=%s&w=%s",from,to, URLEncoder.encode(sourceString,"utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }

    @Override
    boolean isOffline() {
        return false;
    }
}
