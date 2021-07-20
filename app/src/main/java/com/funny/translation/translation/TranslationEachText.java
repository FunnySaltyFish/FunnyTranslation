package com.funny.translation.translation;

import com.funny.translation.bean.Consts;
import com.funny.translation.utils.FunnyEachText;
import com.funny.translation.utils.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class TranslationEachText extends BasicTranslationTask {

    public TranslationEachText(String sourceString, short sourceLanguage, short targetLanguage) {
        super(sourceString, sourceLanguage, targetLanguage);
    }

    @Override
    public String getBasicText(String url) throws TranslationException {
        return sourceString;
    }

    @Override
    public TranslationResult getFormattedResult(String basicText) throws TranslationException {
        if (basicText==null){
            throw new TranslationException(Consts.ERROR_IO);
        }
        String chinese = StringUtil.extraChinese(basicText);
        JSONObject words = null;
        try {
            words = FunnyEachText.getWords();
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < chinese.length(); i++) {
                String each = String.valueOf(chinese.charAt(i));
                if (words.has(each)){
                    stringBuilder.append(words.getString(each));
                }
                stringBuilder.append(" ");
            }
            return new TranslationResult(getEngineKind(),stringBuilder.toString(),null);
        } catch (IOException e) {
            e.printStackTrace();
            throw new TranslationException(Consts.ERROR_IO);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new TranslationException(Consts.ERROR_JSON);
        }
    }

    @Override
    public String madeURL() {
        return null;
    }

    @Override
    public boolean isOffline() {
        return true;
    }

    @Override
    public short getEngineKind() {
        return Consts.ENGINE_EACH_TEXT;
    }
}
