package com.djtiyu.m.djtiyu.util;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * Created by jov on 2015/2/12.
 */
public class FileUtil {
	private static final String ROOT_DIR = "/Djtiyu";
	private static final String IMG_ROOT_DIR = "/Djtiyu/images";
	private static final String CACHE_FILE = ROOT_DIR + "/" + "cache";

	public static File getRootDir() {
		if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState())) {
			File rootFile = new File(Environment.getExternalStorageDirectory() + ROOT_DIR);
			if (!rootFile.exists()) {
				rootFile.mkdir();
			}
			return rootFile;
		}
		return null;
	}

	public static File getCacheFile(String gid, String name) {
		if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState())) {
			File rootFile = new File(Environment.getExternalStorageDirectory() + CACHE_FILE);
			if (!rootFile.exists()) {
				rootFile.mkdirs();
			}
			File retFile = new File(rootFile, gid + "_" + name + ".dat");
			if (!retFile.exists()) {
				try {
					retFile.createNewFile();
				} catch (IOException e) {
					return null;
				}
			}
			return retFile;
		}
		return null;
	}

	public static File getUpdateFile(String code) {
		if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState())) {
			File rootFile = new File(Environment.getExternalStorageDirectory() + ROOT_DIR);
			if (!rootFile.exists()) {
				rootFile.mkdirs();
			}
			File retFile = new File(rootFile, "Djtiyu_" + code + ".apk");
			if (!retFile.exists()) {
				try {
					retFile.createNewFile();
				} catch (IOException e) {
					return null;
				}
			}
			return retFile;
		}
		return null;
	}

	public static File getImageFile(String filename) {
		if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState())) {
			File rootFile = new File(Environment.getExternalStorageDirectory() + IMG_ROOT_DIR);
			if (!rootFile.exists()) {
				rootFile.mkdirs();
			}
			File retFile = new File(rootFile, filename);
			/*
			 * if (!retFile.exists()) { try { retFile.createNewFile(); } catch
			 * (IOException e) { return null; } }
			 */
			return retFile;
		}
		return null;
	}

	public static boolean delImageFile(String filename) {
		if(filename==null){
			return false;
		}
		if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState())) {
			File rootFile = new File(Environment.getExternalStorageDirectory() + IMG_ROOT_DIR);
			if (!rootFile.exists()) {
				rootFile.mkdirs();
			}
			File retFile = new File(rootFile, filename);
			if (!retFile.exists()) {
				return true;
			} else {
				retFile.delete();
			}
			return true;
		}
		return true;
	}
}
