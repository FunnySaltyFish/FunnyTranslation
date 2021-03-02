package com.funny.translation.js;

import com.funny.translation.bean.Consts;

import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSGetter;

public class JSConsts extends ScriptableObject {
    String TAG = "Consts";

    @JSGetter("LANGUAGE_AUTO")
    public int LANGUAGE_AUTO(){
        return Consts.LANGUAGE_AUTO;
    }

    @Override
    public String getClassName() {
        return TAG;
    }
}
