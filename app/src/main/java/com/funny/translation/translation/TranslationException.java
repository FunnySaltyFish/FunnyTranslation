package com.funny.translation.translation;

public class TranslationException extends Exception {
    String errorMessage;

    public TranslationException(String message){
        this.errorMessage=message;
    }

    public String getErrorMessage(){
        return errorMessage;
    }
}
