package com.funny.translation.js;

import com.funny.translation.bean.Consts;
import com.funny.translation.translation.BasicTranslationTask;
import com.funny.translation.translation.TranslationException;
import com.funny.translation.translation.TranslationHelper;
import com.funny.translation.translation.TranslationResult;
import com.funny.translation.utils.StringUtil;

public class TranslationCustom extends BasicTranslationTask {
    JSEngine mJSEngine;


    public TranslationCustom(TranslationHelper helper, String sourceString, short sourceLanguage, short targetLanguage, short engineKind) {
        super(helper, sourceString, sourceLanguage, targetLanguage, engineKind);
    }

    public void setJSEngine(JSEngine jsEngine){
        this.mJSEngine = jsEngine;
    }

    public JSEngine getJSEngine(){return mJSEngine;}

    @Override
    public void translate(short mode){
        result = new TranslationResult(engineKind);
        try {
            mJSEngine.request(this);
            String url = madeURL();
            if (sourceLanguage==targetLanguage){//如果目标语言和源语言相同，跳过翻译
                result.setBasicResult(sourceString);
            }else {
                String basicText = getBasicText(url);
                result = getFormattedResult(basicText);
            }
            result.setStatue(TranslationResult.TRANSLATE_STATUE_SUCCESS);
            onSuccess(helper,result);
        }
        catch (TranslationException e) {
            e.printStackTrace();
            result.setStatue(TranslationResult.TRANSLATE_STATUE_FAIL);
            result.setBasicResult(e.getErrorMessage());
            onFail(helper,result);
        }
    }

    @Override
    public String getBasicText(String url) throws TranslationException {
        Object obj =  mJSEngine.callFunnyJSFunction("getBasicText",new String[]{url});
        String result = "";
        try{
            result = (String)obj;
        }catch (Exception e){
            throw new JSException("JS执行到getBasicText方法时发生过错误！");
        }
        return result;
    }

    @Override
    public TranslationResult getFormattedResult(String basicText) throws TranslationException {
        JSTranslationResult js =  (JSTranslationResult) mJSEngine.callFunnyJSFunction("getFormattedResult",new String[]{basicText});
        return js.getTranslationResult();
    }

    @Override
    public String madeURL(){
        String result = "";
        try{
            Object obj =  mJSEngine.callFunnyJSFunction("madeURL",new Object[]{});
            result = (String)obj;
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean isOffline() {
        try {
            return (Boolean) mJSEngine.callFunnyJSFunction("isOffline", new Object[]{});
        } catch (JSException e) {
            e.printStackTrace();
        }
        return false;
    }
}
