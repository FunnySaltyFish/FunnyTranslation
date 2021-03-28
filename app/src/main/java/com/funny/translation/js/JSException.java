package com.funny.translation.js;

import com.funny.translation.translation.TranslationException;

public class JSException extends TranslationException {
    String mMessage = "";
    public JSException(String message) {
        super(message);
        mMessage = message;
    }

    @Override
    public String getMessage() {
        return mMessage;
    }
}
