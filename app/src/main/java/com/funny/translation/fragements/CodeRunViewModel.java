package com.funny.translation.fragements;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.funny.translation.js.JS;
import com.funny.translation.js.JSEngine;
import com.funny.translation.js.JSException;
import com.funny.translation.utils.LiveDataUtil;

public class CodeRunViewModel extends ViewModel {
    // TODO: Implement the ViewModel
    MutableLiveData<String> output = new MutableLiveData<>("");
    MutableLiveData<JSEngine> jsEngine = new MutableLiveData<>(new JSEngine());

    private JS mJS;

    public void setOutput(String output) {
        //在子线程调用
       LiveDataUtil.setValue(getOutput(),output);
    }

    public MutableLiveData<String> getOutput() {
        return output;
    }

    public void appendOutput(String text){
        LiveDataUtil.setValue(getOutput(),getOutput().getValue()+"【Output】"+text+"\n");
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
}