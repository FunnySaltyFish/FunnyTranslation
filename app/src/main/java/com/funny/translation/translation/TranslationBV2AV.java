package com.funny.translation.translation;

import android.text.TextUtils;

import com.funny.translation.bean.Consts;
import com.funny.translation.utils.FunnyBvToAv;
import com.funny.translation.utils.StringUtil;

public class TranslationBV2AV extends BasicTranslationTask{

    public TranslationBV2AV(String sourceString, short sourceLanguage, short targetLanguage) {
        super(sourceString, sourceLanguage, targetLanguage);
    }

    @Override
    public String getBasicText(String url) throws TranslationException {
        String result="";
        String inputText = sourceString;
        if (TextUtils.isEmpty(inputText))
        {
            return result;
        }
        if (StringUtil.isNumber(inputText))
        {
            result = FunnyBvToAv.enc(Long.parseLong(inputText));
        }else{
            long av=0;
            if ((av = StringUtil.findAv(inputText)) > 0){
                result = FunnyBvToAv.enc(av);
            }else{
                String bv="";
                if (!(bv = StringUtil.findBv(inputText)).equals("")){
                    result = FunnyBvToAv.dec(bv);
                }
            }
        }
        return result;
    }

    @Override
    public TranslationResult getFormattedResult(String basicText) throws TranslationException {
        TranslationResult result = new TranslationResult(getEngineKind());
        if (!basicText.equals("")){
            result.setBasicResult(basicText);
        }else{
            throw new TranslationException(Consts.ERROR_NO_BV_OR_AV);
        }
        return result;
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
        return Consts.ENGINE_BV_TO_AV;
    }
}
