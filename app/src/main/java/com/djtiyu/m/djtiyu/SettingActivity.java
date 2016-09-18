package com.djtiyu.m.djtiyu;

import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.djtiyu.m.djtiyu.util.Callback;
import com.djtiyu.m.djtiyu.util.CommonUtil;
import com.djtiyu.m.djtiyu.util.Constants;
import com.djtiyu.m.djtiyu.util.CustomProgressDialog;
import com.djtiyu.m.djtiyu.util.NetworkHandler;
import com.djtiyu.m.djtiyu.util.TransResp;
import com.djtiyu.m.djtiyu.util.UpdateBean;
import com.djtiyu.m.djtiyu.util.UpdateManagerService;
import com.google.gson.Gson;

import org.apache.http.HttpStatus;

import java.io.File;

/**
 * Created by shuwei on 16/9/7.
 */
public class SettingActivity extends BaseActivity implements View.OnClickListener {
  private View vLeftBtn, vClearBtn, vCheckBtn;
  private Handler handler = new Handler();
  private TextView vVersionTxt;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_setting);
    initView();
  }

  private void initView() {
    vLeftBtn = findViewById(R.id.leftBtn);
    vClearBtn = findViewById(R.id.clear_lay);
    vCheckBtn = findViewById(R.id.check_version_lay);
    vVersionTxt = (TextView) findViewById(R.id.check_version_txt);
    vLeftBtn.setOnClickListener(this);
    vClearBtn.setOnClickListener(this);
    vCheckBtn.setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    if (v == vLeftBtn) {
      finish();
    }
    if (v == vClearBtn) {
      if (progressDialog == null) {
        progressDialog = CustomProgressDialog.show(this, "正在清理...", false);
      }
      progressDialog.setMsg("正在清理...");
      progressDialog.show();
      handler.postDelayed(runnable, 3000);
      return;
    } else if (v == vCheckBtn) {
      checkVersion();
    }
  }

  private Runnable runnable = new Runnable() {
    @Override
    public void run() {
      clearWebViewCache();
    }
  };


  public void clearWebViewCache() {
    //清理Webview缓存数据库
    try {
      deleteDatabase("webview.db");
      deleteDatabase("webviewCache.db");
    } catch (Exception e) {
      e.printStackTrace();
    }
    File cacheDir = new File(getFilesDir().getAbsolutePath());
    //WebView 缓存文件
    File appCacheDir = new File(getFilesDir().getAbsolutePath() + MainActivity.APP_CACAHE_DIRNAME);
    File webviewCacheDir = new File(getCacheDir().getAbsolutePath() + "/webviewCache");
    //删除webview 缓存目录
    if (webviewCacheDir.exists()) {
      deleteFile(webviewCacheDir);
    }
    //删除webview 缓存 缓存目录
    if (appCacheDir.exists()) {
      deleteFile(appCacheDir);
    }
    //删除webview 缓存 缓存目录
    if (cacheDir.exists()) {
      deleteFile(cacheDir);
    }
    Message message = new Message();
    message.what = 100;
    MainActivity.mHandler.sendMessage(message);
    progressDialog.dismiss();
    Toast.makeText(this, "清理完成", Toast.LENGTH_SHORT).show();
  }

  /**
   * 递归删除 文件/文件夹
   *
   * @param file
   */
  public void deleteFile(File file) {

    if (file.exists()) {
      if (file.isFile()) {
        file.delete();
      } else if (file.isDirectory()) {
        File files[] = file.listFiles();
        for (int i = 0; i < files.length; i++) {
          deleteFile(files[i]);
        }
      }
      file.delete();
    }
  }


  private String version;

  private void getVersion() {
    PackageManager manager = this.getPackageManager();
    PackageInfo info = null;
    try {
      info = manager.getPackageInfo(this.getPackageName(), 0);
      if (info != null) {
        version = info.versionName;
      }
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
  }

  private void checkVersion() {
    if (!CommonUtil.isNetWorkConnected(this)) {
      showSimpleMessageDialog("网络无法连接");
      return;
    }
    if (progressDialog == null) {
      progressDialog = new CustomProgressDialog(this, "正在检查...", false);
      progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialogInterface) {
          dialogInterface.dismiss();
        }
      });

    }
    progressDialog.setMsg("正在检查...");
    progressDialog.show();
    NetworkHandler networkHandler = NetworkHandler.getInstance();
    getVersion();
    networkHandler.post(Constants.VER_URL, null, 30, new Callback<TransResp>() {
      @Override
      public void callback(TransResp transResp) {

        if (transResp.getRetcode() == HttpStatus.SC_OK) {
          String ret = transResp.getRetjson();
          if (CommonUtil.isEmpty(ret)) {
            progressDialog.dismiss();
            showSimpleMessageDialog("请求失败了，请稍后再试！");
            return;
          }
          Gson gson = new Gson();
          try {
            UpdateBean bean = gson.fromJson(ret, UpdateBean.class);
            if (bean != null && !CommonUtil.isEmpty(bean.getVersion()) && !CommonUtil.isEmpty(bean.getDownload())) {
              if (String.valueOf(version).compareToIgnoreCase(bean.getVersion()) < 0) {
                UpdateManagerService service = new UpdateManagerService(SettingActivity.this);
                progressDialog.dismiss();
                service.doUpdate(bean.getDownload(), bean.getMessage(), bean.getVersion());
              } else {
                progressDialog.dismiss();
                vVersionTxt.setText("已是最新版");
              }
            }
          } catch (Exception e) {
            e.printStackTrace();
            progressDialog.dismiss();
            showSimpleMessageDialog("请求失败了，请稍后再试！");
            return;
          }
        } else if (0 == transResp.getRetcode()) {
          progressDialog.dismiss();
          showSimpleMessageDialog("请求超时了，请稍后再试！");
          return;
        } else {
          progressDialog.dismiss();
          showSimpleMessageDialog("请求失败了，请稍后再试！");
        }
      }
    });
  }
}
