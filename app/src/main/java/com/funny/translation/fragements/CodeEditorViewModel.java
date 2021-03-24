package com.funny.translation.fragements;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CodeEditorViewModel extends ViewModel {
    // TODO: Implement the ViewModel
    MutableLiveData<String> code = new MutableLiveData<>("");
    MutableLiveData<String[]> keywords = new MutableLiveData<>(new String[]{});

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
}