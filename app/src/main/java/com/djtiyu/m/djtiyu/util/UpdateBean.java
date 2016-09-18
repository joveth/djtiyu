package com.djtiyu.m.djtiyu.util;

/**
 * Created by jov on 2015/3/27.
 */
public class UpdateBean {

	private String version;
	private String message;
	private String download;

	public String getDownload() {
		return download;
	}

	public void setDownload(String download) {
		this.download = download;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
