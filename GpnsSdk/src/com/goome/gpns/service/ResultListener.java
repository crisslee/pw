package com.goome.gpns.service;

public interface ResultListener<T> {
	void onSuccessed(T result);

	void onFailed(String error);
}
