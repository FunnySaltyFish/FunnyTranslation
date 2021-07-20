package com.funny.translation.js;

import com.funny.translation.bean.Consts;
import com.funny.translation.translation.BasicTranslationTask;
import com.funny.translation.translation.TranslationException;
import com.funny.translation.translation.NewTranslationHelper;
import com.funny.translation.translation.TranslationResult;

import org.mozilla.javascript.NativeJavaObject;

public class TranslationCustom extends BasicTranslationTask {
    JSEngine mJSEngine;

    public TranslationCustom(String sourceString, short sourceLanguage, short targetLanguage) {
        super(sourceString, sourceLanguage, targetLanguage);
    }

    public void setJSEngine(JSEngine jsEngine){
        this.mJSEngine = jsEngine;
    }

    public JSEngine getJSEngine(){return mJSEngine;}

    @Override
    public void translate(short mode) throws TranslationException {
        result = new TranslationResult(getEngineKind());

        JSManager.currentRunningJSEngine = mJSEngine;
        try {
            mJSEngine.request(this);
            String url = madeURL();
            if (sourceLanguage==targetLanguage){//如果目标语言和源语言相同，跳过翻译
                result.setBasicResult(sourceString);
            }else {
                String basicText = getBasicText(url);
                result = getFormattedResult(basicText);
            }
            result.setSourceString(sourceString);
            result.setStatue(TranslationResult.TRANSLATE_STATUE_SUCCESS);
        }
        catch (TranslationException e) {
            e.printStackTrace();
            result.setSourceString(sourceString);
            result.setStatue(TranslationResult.TRANSLATE_STATUE_FAIL);
            result.setBasicResult(e.getErrorMessage());
            throw e;
        }
    }

    @Override
    public String getBasicText(String url) throws TranslationException {
        String result = "";
        try{
            Object obj =  mJSEngine.callFunnyJSFunction("getBasicText",new String[]{url});
            if (obj instanceof NativeJavaObject){
                result = (String)((NativeJavaObject) obj).unwrap();
            }else result = (String)obj;
        }catch (JSException e){
            throw e;
        } catch (Exception e){
            e.printStackTrace();
            throw new JSException("JS执行到getBasicText方法时发生过错误！"+(mJSEngine.js.isDebugMode?"详细原因是："+e.getMessage():""));
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
            if (obj instanceof NativeJavaObject){
                result = (String)((NativeJavaObject) obj).unwrap();
            }else result = (String)obj;
        }
        catch (Exception e){
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

    @Override
    public short getEngineKind() {
        return Consts.ENGINE_JS;
    }
}
