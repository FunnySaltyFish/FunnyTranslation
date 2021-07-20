package com.funny.translation.translation;

import com.funny.translation.bean.Consts;
import com.funny.translation.utils.StringUtil;

public abstract class BasicTranslationTask{
    private static final String TAG = "BasicTranslationTask";

    public String sourceString;

    public short sourceLanguage;
    public short targetLanguage;

    public TranslationResult result;

    public abstract String getBasicText(String url) throws TranslationException;
    public abstract TranslationResult getFormattedResult(String basicText) throws TranslationException;

    public abstract String madeURL();
    public abstract boolean isOffline();
    public abstract short getEngineKind();

    public BasicTranslationTask(String sourceString, short sourceLanguage, short targetLanguage) {
        this.sourceString = sourceString;
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
    }

    public void translate(short mode) throws TranslationException {
        String url = madeURL();
        String processedString = null;
        short engineKind = getEngineKind();
        result = new TranslationResult(engineKind);
        try {
            if (mode==Consts.MODE_EACH_TEXT&&sourceLanguage!=Consts.LANGUAGE_CHINESE){
                throw new TranslationException(Consts.ERROR_ONLY_CHINESE_SUPPORT);
            }
            if(engineKind==Consts.ENGINE_BIGGER_TEXT&&!StringUtil.isValidContent(StringUtil.extraChinese(sourceString))){
                throw new TranslationException(Consts.ERROR_ONLY_CHINESE_SUPPORT);
            }
            processedString = getProcessedString(sourceString,mode);
            //Log.i(TAG,"获取到的processedString是"+processedString);
            result.setSourceString(processedString);
            if (sourceLanguage==targetLanguage){//如果目标语言和源语言相同，跳过翻译
                result.setBasicResult(sourceString);
            }else {
                String basicText = getBasicText(url);
                result = getFormattedResult(basicText);
            }
            reFormatBasicText(result,mode);//还原处理过的basicText
            result.setSourceString(sourceString);
            result.setStatue(TranslationResult.TRANSLATE_STATUE_SUCCESS);
        }
        catch (TranslationException e) {
            e.printStackTrace();
            reFormatBasicText(result,mode);//还原处理过的basicText
            result.setSourceString(sourceString);
            result.setStatue(TranslationResult.TRANSLATE_STATUE_FAIL);
            result.setBasicResult(e.getErrorMessage());
            throw e;
        }
    }

    private String getProcessedString (String str,short mode) throws TranslationException{
        String result = "";
        //根据模式预处理源文本
        switch (mode){
            case Consts.MODE_EACH_TEXT:
                String chinese = StringUtil.extraChinese(str);
                if (!StringUtil.isValidContent(chinese)){
                    throw new TranslationException(Consts.ERROR_ONLY_CHINESE_SUPPORT);
                }
                result = StringUtil.insertJuhao(chinese);
                break;
            default:
                result=str;
        }
        return result;
    }

    private void reFormatBasicText(TranslationResult translationResult,short mode){
        switch (mode){
            case Consts.MODE_EACH_LINE:
                StringBuilder sb = new StringBuilder();
                String[] sourceArr = sourceString.split("\n");
                String[] targetArr = translationResult.getBasicResult().split("\n");
                int length = Math.min(sourceArr.length,targetArr.length);
                for (int i = 0; i < length; i++) {
                    sb.append(sourceArr[i]);
                    sb.append("\n");
                    sb.append(targetArr[i]);
                    sb.append("\n");
                }
                sb.deleteCharAt(sb.length()-1);
                translationResult.setBasicResult(sb.toString());
                break;
            case Consts.MODE_EACH_TEXT:
                String basicResult = translationResult.getBasicResult();
                //Log.i(TAG,"获取到的basicResult :"+basicResult);
                String reFormattedString = basicResult.replaceAll("。","");
                reFormattedString = reFormattedString.replaceAll(","," ");
                translationResult.setBasicResult(reFormattedString);
                //Log.i(TAG,"最后设置的basicResult is "+translationResult.getBasicResult());

                break;
            default:
                break;
        }
    }

    public TranslationResult getResult() {
        return result;
    }
}
