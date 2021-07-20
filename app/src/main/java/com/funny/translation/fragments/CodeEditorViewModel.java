package com.funny.translation.fragments;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CodeEditorViewModel extends ViewModel {
    // TODO: Implement the ViewModel
    MutableLiveData<String> code = new MutableLiveData<>("");
    MutableLiveData<String[]> keywords = new MutableLiveData<>(new String[]{});

    long curBackTime = 0, firstBackTime = 0;

    public MutableLiveData<String> getCode() {
        return code;
    }

    public void setCode(String code) {
        getCode().setValue(code);
    }

    public void setKeywords(String[] keywords) {
        getKeywords().setValue(keywords);
    }

    public MutableLiveData<String[]> getKeywords() {
        return keywords;
    }

    public long getCurBackTime() {
        return curBackTime;
    }

    public void setCurBackTime(long curBackTime) {
        this.curBackTime = curBackTime;
    }

    public long getFirstBackTime() {
        return firstBackTime;
    }

    public void setFirstBackTime(long firstBackTime) {
        this.firstBackTime = firstBackTime;
    }

}