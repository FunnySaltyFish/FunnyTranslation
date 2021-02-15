package com.funny.translation.js;

import com.funny.translation.translation.TranslationResult;

import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSConstructor;
import org.mozilla.javascript.annotations.JSFunction;
import org.mozilla.javascript.annotations.JSGetter;
import org.mozilla.javascript.annotations.JSSetter;

public class JSTranslationResult extends ScriptableObject {
    private String TAG = "JSTranslationResult";
    private TranslationResult mTranslationResult;

    @JSConstructor
    public void constructor(String sourceString){
        mTranslationResult = new TranslationResult(JSManager.getJSEngineKind(),sourceString);
    }

    @JSSetter("basicText")
    public void setBasicText(String basicText){
        mTranslationResult.setBasicResult(basicText);
    }

    @JSGetter("basicText")
    public String getBasicText(){
        return mTranslationResult.getBasicResult();
    }

    public TranslationResult getTranslationResult(){
        return mTranslationResult;
    }

    @Override
    public String getClassName() {
        return TAG;
    }
}
