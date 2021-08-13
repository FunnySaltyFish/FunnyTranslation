package com.funny.translation.translation;

import com.funny.translation.trans.CoreTranslationTask;

public interface OnTranslateListener{
	void finishOne(CoreTranslationTask task, Exception e);
	void finishAll();
}
