package com.djtiyu.m.djtiyu.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by jov on 2015/1/16.
 */
public class CommonUtil {
	public static String getNowDate() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:ss:mm");
		return format.format(new Date());
	}

	public static String getNowDateNoFormat() {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHssmm");
		return format.format(new Date());
	}

	public static boolean isEmpty(String str) {
		return str == null || str.trim().length() == 0;
	}

	public static boolean isNetWorkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	public static String encodingMD5(String val) {
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("MD5 should be unsupported", e);
		}
		md5.update(val.getBytes());
		byte[] m = md5.digest();// 加密
		return getString(m);
	}

	private static String getString(byte[] b) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			sb.append(b[i]);
		}
		return sb.toString();
	}

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	public static int px2sp(Context context, float pxValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (pxValue / fontScale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static String dateStrFormat(String str) {
		if (str == null)
			return null;
		if (str.length() == 6)
			return str.substring(0, 4) + "-" + str.substring(4, 6);
		if (str.length() == 8)
			return str.substring(0, 4) + "-" + str.substring(4, 6) + "-" + str.substring(6, 8);

		if (str.length() == 10)
			return str.substring(0, 4) + "-" + str.substring(4, 6) + "-" + str.substring(6, 8) + " " + str.substring(8, 10);

		if (str.length() == 12)
			return str.substring(0, 4) + "-" + str.substring(4, 6) + "-" + str.substring(6, 8) + " " + str.substring(8, 10)
					+ ":" + str.substring(10, 12);
		if (str.length() == 14)
			return str.substring(0, 4) + "-" + str.substring(4, 6) + "-" + str.substring(6, 8) + " " + str.substring(8, 10)
					+ ":" + str.substring(10, 12) + ":" + str.substring(12, 14);
		return str;
	}

	// xx月xx日
	public static String dateMonthFormatZH(String str) {
		if (str == null)
			return null;
		if (str.length() == 6)
			return str.substring(0, 4) + "年" + str.substring(4, 6) + "月";
		if (str.length() >= 8)
			return str.substring(4, 6) + "月" + str.substring(6, 8) + "日";
		return str;
	}

	// xx-xx
	public static String dateMonthFormatEN(String str) {
		if (str == null)
			return null;
		if (str.length() == 6)
			return str.substring(0, 4) + "-" + str.substring(4, 6);
		if (str.length() >= 8)
			return str.substring(4, 6) + "-" + str.substring(6, 8);
		return str;
	}

}
