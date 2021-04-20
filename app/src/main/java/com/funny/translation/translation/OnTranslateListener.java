package com.funny.translation.translation;

public interface OnTranslateListener{
	void onSuccess(NewTranslationHelper helper, TranslationResult result);
	void onFail(NewTranslationHelper helper, TranslationResult result);
}
