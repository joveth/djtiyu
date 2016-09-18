package com.djtiyu.m.djtiyu.util;

import org.apache.http.NameValuePair;

import java.util.List;

public class NetworkHandler {

	private static NetworkHandler instance;

	public static synchronized NetworkHandler getInstance() {
		if (instance == null) {
			instance = new NetworkHandler();
		}
		return instance;
	}

	public void post(final String url, List<NameValuePair> paramspost, int timeout, final Callback<TransResp> callback) {
		new PostTask(url, paramspost, timeout, callback).execute();
	}
}