package com.funny.translation.jetpack;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ActivityCodeViewModel extends ViewModel {
    MutableLiveData<String> code = new MutableLiveData<>("");

    public MutableLiveData<String> getCode() {
        return code;
    }

    public void setCode(String code) {
        getCode().setValue(code);
    }

}
