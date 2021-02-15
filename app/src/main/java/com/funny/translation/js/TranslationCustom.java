package com.funny.translation.js;

import com.funny.translation.translation.BasicTranslationTask;
import com.funny.translation.translation.TranslationException;
import com.funny.translation.translation.TranslationHelper;
import com.funny.translation.translation.TranslationResult;

public class TranslationCustom extends BasicTranslationTask {
    JSEngine mJSEngine;

    public TranslationCustom(TranslationHelper helper, String sourceString, short sourceLanguage, short targetLanguage, short engineKind) {
        super(helper, sourceString, sourceLanguage, targetLanguage, engineKind);
    }

    public void setJSEngine(JSEngine jsEngine){
        this.mJSEngine = jsEngine;
    }

    @Override
    public String getBasicText(String url) throws TranslationException {
        return mJSEngine.callFunctionReturnString("getBasicText",new String[]{url});
    }

    @Override
    public TranslationResult getFormattedResult(String basicText) throws TranslationException {
        JSTranslationResult js =  (JSTranslationResult) mJSEngine.callFunction("getFormattedResult",new String[]{basicText});
        return js.getTranslationResult();
    }

    @Override
    public String madeURL(){
        try {
            return mJSEngine.callFunctionReturnString("madeURL",null);
        } catch (JSException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public boolean isOffline() {
        try {
            return (Boolean) mJSEngine.callFunction("isOffline", null);
        } catch (JSException e) {
            e.printStackTrace();
        }
        return false;
    }
}
