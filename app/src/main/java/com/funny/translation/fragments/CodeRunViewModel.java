package com.funny.translation.fragments;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.funny.translation.bean.Consts;
import com.funny.translation.js.JS;
import com.funny.translation.js.JSEngine;
import com.funny.translation.js.JSException;

public class CodeRunViewModel extends ViewModel {
    // TODO: Implement the ViewModel
    MutableLiveData<String> output = new MutableLiveData<>("");
    MutableLiveData<JSEngine> jsEngine = new MutableLiveData<>(new JSEngine());

    short sourceLanguage=Consts.LANGUAGE_CHINESE,targetLanguage=Consts.LANGUAGE_ENGLISH;
    String sourceString="你好";

    private JS mJS;

    public void setOutput(String output) {
        //在子线程调用
       getOutput().setValue(output);
    }

    public MutableLiveData<String> getOutput() {
        return output;
    }

    public void appendOutput(String text){
        getOutput().setValue(getOutput().getValue()+"【Output】"+text+"\n");
    }

    public void appendDividerLine(){
        appendOutput("================");
    }

    public MutableLiveData<JSEngine> getJsEngine() {
        return jsEngine;
    }

    public void reloadJS(String jsCode) throws JSException {
        if(mJS == null){
            mJS = new JS(jsCode);
        }else mJS.code = jsCode;
        getJsEngine().getValue().loadJS(mJS);
        getJsEngine().getValue().loadBasicConfigurations();
    }

    public void clearOutput() {
        setOutput("");
    }

    public short getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(short sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public short getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(short targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    public String getSourceString() {
        return sourceString;
    }

    public void setSourceString(String sourceString) {
        this.sourceString = sourceString;
    }
}