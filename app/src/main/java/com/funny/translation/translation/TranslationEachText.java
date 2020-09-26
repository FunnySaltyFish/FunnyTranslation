package com.funny.translation.translation;

import com.funny.translation.bean.Consts;
import com.funny.translation.utils.FunnyEachText;
import com.funny.translation.utils.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class TranslationEachText extends BasicTranslationTask {
    public TranslationEachText(TranslationHelper helper, String sourceString, short sourceLanguage, short targetLanguage, short engineKind) {
        super(helper, sourceString, sourceLanguage, targetLanguage, engineKind);
    }

    @Override
    String getBasicText(String url) throws TranslationException {
        return sourceString;
    }

    @Override
    TranslationResult getFormattedResult(String basicText) throws TranslationException {
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
            return new TranslationResult(engineKind,stringBuilder.toString(),null);
        } catch (IOException e) {
            e.printStackTrace();
            throw new TranslationException(Consts.ERROR_IO);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new TranslationException(Consts.ERROR_JSON);
        }
    }

    @Override
    String madeURL() {
        return null;
    }

    @Override
    boolean isOffline() {
        return true;
    }
}
