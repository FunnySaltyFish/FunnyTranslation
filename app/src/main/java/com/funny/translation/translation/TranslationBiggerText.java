package com.funny.translation.translation;

import com.funny.translation.FunnyApplication;
import com.funny.translation.utils.FunnyBiggerText;

public class TranslationBiggerText extends  BasicTranslationTask {
    public TranslationBiggerText(TranslationHelper helper, String sourceString, short sourceLanguage, short targetLanguage, short engineKind) {
        super(helper, sourceString, sourceLanguage, targetLanguage, engineKind);
    }

    @Override
    String getBasicText(String url) throws TranslationException {
        return sourceString;
    }

    @Override
    TranslationResult getFormattedResult(String basicText) throws TranslationException {
        return new TranslationResult(engineKind,FunnyBiggerText.drawString(FunnyApplication.getFunnyContext(),basicText),new String[1][1]);
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
