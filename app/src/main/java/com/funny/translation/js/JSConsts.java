package com.funny.translation.js;

import com.funny.translation.bean.Consts;

import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSGetter;

public class JSConsts extends ScriptableObject {
    String TAG = "JSConsts";

    @JSGetter("LANGUAGE_AUTO")
    public int LANGUAGE_AUTO(){
        return Consts.LANGUAGE_AUTO;
    }

    @JSGetter("LANGUAGE_CHINESE")
    public int LANGUAGE_CHINESE(){
        return Consts.LANGUAGE_CHINESE;
    }

    @JSGetter("LANGUAGE_ENGLISH")
    public int LANGUAGE_ENGLISH(){
        return Consts.LANGUAGE_ENGLISH;
    }

    @JSGetter("LANGUAGE_JAPANESE")
    public int LANGUAGE_JAPANESE(){
        return Consts.LANGUAGE_JAPANESE;
    }

    @JSGetter("LANGUAGE_KOREAN")
    public int LANGUAGE_KOREAN(){
        return Consts.LANGUAGE_KOREAN;
    }

    @JSGetter("LANGUAGE_FRENCH")
    public int LANGUAGE_FRENCH(){
        return Consts.LANGUAGE_FRENCH;
    }

    @JSGetter("LANGUAGE_RUSSIAN")
    public int LANGUAGE_RUSSIAN(){
        return Consts.LANGUAGE_RUSSIAN;
    }

    @JSGetter("LANGUAGE_GERMANY")
    public int LANGUAGE_GERMANY(){
        return Consts.LANGUAGE_GERMANY;
    }

    @JSGetter("LANGUAGE_WENYANWEN")
    public int LANGUAGE_WENYANWEN(){
        return Consts.LANGUAGE_WENYANWEN;
    }

    @JSGetter("LANGUAGE_THAI")
    public int LANGUAGE_THAI(){return Consts.LANGUAGE_THAI;}

    @Override
    public String getClassName() {
        return TAG;
    }
}
