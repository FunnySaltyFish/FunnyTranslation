package com.funny.translation.translation;

public interface OnTranslateListener{
	void onSuccess(String source,String result);
	void onFail(String reason);
}
