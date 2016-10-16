package com.djtiyu.m.djtiyu.util;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.widget.RemoteViews;

import com.djtiyu.m.djtiyu.MainActivity;
import com.djtiyu.m.djtiyu.R;
import com.google.gson.Gson;

import org.apache.http.HttpStatus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by jov on 2015/1/30.
 */
public class UpdateManagerService {
	private Context context;
	private int versioncode;
	private String downUrl, versionName, newVersionName;
	private static final int DOWN_OK = 21;
	private static final int DOWN_ERROR = 20;
	private File updateFile;
	private Intent updateIntent;
	private NotificationManager notificationManager;
	private Notification notification;
	private PendingIntent pendingIntent;
	private int notification_id = 0;
	private static final int TIMEOUT = 30 * 1000;// 超时

	public UpdateManagerService(Context context) {
		this.context = context;
	}

	public void checkVersion() {
		if (!CommonUtil.isNetWorkConnected(context)) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				getAppVersionNameOr();
			}
		}).start();
	}

	public String getVersionName() {
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			versionName = pi.versionName;
			versioncode = pi.versionCode;
			if (versionName == null || versionName.length() <= 0) {
				return "";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return versionName;
	}

	public void getAppVersionNameOr() {
		getVersionName();
		if (CommonUtil.isEmpty(versionName)) {
			return;
		}
		NetworkHandler networkHandler = NetworkHandler.getInstance();
		networkHandler.post(Constants.VER_URL, null, 30, new Callback<TransResp>() {
			@Override
			public void callback(TransResp transResp) {
				if (transResp.getRetcode() == HttpStatus.SC_OK) {
					String ret = transResp.getRetjson();
					if (!CommonUtil.isEmpty(ret)) {
						Gson gson = new Gson();
						try {
							UpdateBean bean = gson.fromJson(ret, UpdateBean.class);
							if (bean != null && !CommonUtil.isEmpty(bean.getVersion()) && !CommonUtil.isEmpty(bean.getDownload())) {
								if (versionName.compareToIgnoreCase(bean.getVersion()) < 0) {
									doUpdate(bean.getDownload(), bean.getMessage(), bean.getVersion());
									Message message = new Message();
									message.what = 100;
									message.obj = bean.getVersion();
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} else {
				}
			}
		});
	}

	public void doUpdate(String url, String updateContent, String newVersionName) {
		downUrl = url;
		this.newVersionName = newVersionName;
		if (CommonUtil.isEmpty(downUrl)) {
			return;
		}
		showDialog(updateContent);
	}

	private void downLoadThread() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				updateFile = FileUtil.getUpdateFile(newVersionName);
				long downloadSize = downloadUpdateFile(downUrl, updateFile);
				Message message = new Message();
				if (downloadSize > 0) {
					// 下载成功
					message.what = DOWN_OK;
					handler.sendMessage(message);
				} else {
					message.what = DOWN_ERROR;
					handler.sendMessage(message);
				}
			}
		}).start();
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DOWN_OK:
				// 下载完成，点击安装
				if (updateFile != null) {
					Uri uri = Uri.fromFile(updateFile);
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setDataAndType(uri, "application/vnd.android.package-archive");
					pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
					notification.tickerText = context.getString(R.string.app_name) + "下载完成，点击安装";
          notification = new Notification.Builder(context)
              .setAutoCancel(true)
              .setContentTitle(context.getString(R.string.app_name))
              .setContentText("下载完成，点击安装")
              .setContentIntent(pendingIntent)
              .setSmallIcon(R.mipmap.ic_launcher)
              .setWhen(System.currentTimeMillis())
              .build();
					notificationManager.notify(notification_id, notification);
					context.startActivity(intent);
					// 如果不加上这句的话在apk安装完成之后点击单开会崩溃
					android.os.Process.killProcess(android.os.Process.myPid());
					break;
				}
			case DOWN_ERROR:
        notification = new Notification.Builder(context)
            .setAutoCancel(true)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText("下载失败")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setWhen(System.currentTimeMillis())
            .build();
				break;
			default:
				break;
			}
		}
	};

	private void showDialog(String msg) {
		// 发现新版本，提示用户更新
		AlertDialog.Builder alert = new AlertDialog.Builder(context,AlertDialog.THEME_HOLO_LIGHT);
		if (CommonUtil.isEmpty(msg)) {
			msg = "新版本更新";
		}
		alert.setTitle("发现新的版本").setMessage(msg).setPositiveButton("现在更新", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				createNotification();
				downLoadThread();
			}
		}).setNegativeButton("下次再说", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).setCancelable(false);
		alert.create().show();
	}

	private RemoteViews contentView;

	private void createNotification() {
		notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notification = new Notification();
		notification.icon = R.mipmap.ic_launcher;
		notification.tickerText = context.getString(R.string.app_name) + "更新";
		contentView = new RemoteViews(context.getPackageName(), R.layout.notification_update);
		contentView.setTextViewText(R.id.notificationTitle, "正在下载");
		contentView.setTextViewText(R.id.notificationPercent, "0%");
		contentView.setProgressBar(R.id.notificationProgress, 100, 0, false);
		notification.contentView = contentView;
		updateIntent = new Intent(context, MainActivity.class);
		updateIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		pendingIntent = PendingIntent.getActivity(context, 0, updateIntent, 0);
		notification.contentIntent = pendingIntent;
		notificationManager.notify(notification_id, notification);
	}

	private long downloadUpdateFile(String down_url, File file) {
		if (file == null) {
			return 0;
		}
		int down_step = 5;// 提示step
		int totalSize;// 文件总大小
		int downloadCount = 0;// 已经下载好的大小
		int updateCount = 0;// 已经上传的文件大小
		InputStream inputStream;
		OutputStream outputStream;
		URL url;
		try {
			url = new URL(down_url);
			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			httpURLConnection.setConnectTimeout(TIMEOUT);
			httpURLConnection.setReadTimeout(TIMEOUT);
			// 获取下载文件的size
			totalSize = httpURLConnection.getContentLength();
			if (httpURLConnection.getResponseCode() != HttpStatus.SC_OK) {
				httpURLConnection.disconnect();
				return 0;
			}
			inputStream = httpURLConnection.getInputStream();
			outputStream = new FileOutputStream(file, false);// 文件存在则覆盖掉
			byte buffer[] = new byte[1024];
			int readsize = 0;
			while ((readsize = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, readsize);
				downloadCount += readsize;// 时时获取下载到的大小
				if (updateCount == 0 || (downloadCount * 100 / totalSize - down_step) >= updateCount) {
					updateCount += down_step;
					contentView.setTextViewText(R.id.notificationPercent, updateCount + "%");
					contentView.setProgressBar(R.id.notificationProgress, 100, updateCount, false);
					// show_view
					notificationManager.notify(notification_id, notification);
				}
			}
			if (httpURLConnection != null) {
				httpURLConnection.disconnect();
			}
			inputStream.close();
			outputStream.close();
			return downloadCount;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
}
