package com.djtiyu.m.djtiyu.util;

import com.djtiyu.m.djtiyu.db.NameValuePair;

import org.apache.http.params.HttpParams;

import java.util.List;

public class NetworkHandler {

	private static NetworkHandler instance;

	public static synchronized NetworkHandler getInstance() {
		if (instance == null) {
			instance = new NetworkHandler();
		}
		return instance;
	}
	public void get(final String url, final HttpParams queryParams, int timeout, final Callback<TransResp> callback) {
		new GetTask(url, queryParams, 15, callback).execute();
	}
	public void post(final String url, List<NameValuePair> paramspost, int timeout, final Callback<TransResp> callback) {
		new PostTask(url, paramspost, timeout, callback).execute();
	}
	public void postJson(final String url, String paramspost, int timeout, final Callback<TransResp> callback) {
		new JsonPostTask(url, paramspost, timeout, callback).execute();
	}
}