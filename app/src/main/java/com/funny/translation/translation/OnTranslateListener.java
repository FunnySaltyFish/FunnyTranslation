package com.funny.translation.translation;

public interface OnTranslateListener{
	void finishOne(BasicTranslationTask task,Exception e);
	void finishAll();
}
