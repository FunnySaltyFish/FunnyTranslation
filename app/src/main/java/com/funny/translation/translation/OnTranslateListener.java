package com.funny.translation.translation;

public interface OnTranslateListener{
	void onSuccess(TranslationHelper helper,TranslationResult result);
	void onFail(TranslationHelper helper,TranslationResult result);
}
