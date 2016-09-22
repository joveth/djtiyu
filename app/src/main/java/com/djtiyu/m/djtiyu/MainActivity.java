package com.djtiyu.m.djtiyu;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.actionsheet.ActionSheet;
import com.djtiyu.m.djtiyu.db.BeanPropEnum;
import com.djtiyu.m.djtiyu.util.Callback;
import com.djtiyu.m.djtiyu.util.CommonUtil;
import com.djtiyu.m.djtiyu.util.Constants;
import com.djtiyu.m.djtiyu.util.CustomProgressDialog;
import com.djtiyu.m.djtiyu.util.FileUtil;
import com.djtiyu.m.djtiyu.util.TransResp;
import com.djtiyu.m.djtiyu.util.UpdateManagerService;

import org.apache.http.NameValuePair;
import org.apache.http.cookie.Cookie;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

public class MainActivity extends BaseActivity implements ActionSheet.ActionSheetListener {

  public static final String APP_CACAHE_DIRNAME = "/webcache";
  private View vLeftBtn, vRightBtn, vNoNetWork, vRetryBtn,vTopBar;
  private TextView vTitleTxt;
  private WebView webView;
  private ProgressBar bar;
  private boolean hasLoaded = false, loginedOnce, logined, needBackHome;
  private ValueCallback<Uri> mUploadMessage;
  private ValueCallback<Uri[]> mUploadMessageForAndroid5;
  private ActionSheet.Builder actionSheet;
  private ActionSheet actionSheet1;
  private boolean isShowing;
  private static final int CAMERA_REQUEST_CODE = 1;
  private static final int RESULT_REQUEST_CODE = 2;
  private static final int PHOTO_REQUEST_CODE = 3;
  private String lastUrl, acc, pwd;
  private File tempFile;
  private Uri uri;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    initView();
    UpdateManagerService updateManagerService = new UpdateManagerService(this);
    updateManagerService.checkVersion();
    initOther();
    JPushInterface.setDebugMode(true);
    JPushInterface.init(this);
    registerMessageReceiver();
  }

  private void initView() {
    vTopBar = findViewById(R.id.topBar);
    vLeftBtn = findViewById(R.id.leftBtn);
    vLeftBtn.setOnClickListener(this);
    vTitleTxt = (TextView) findViewById(R.id.titleTxt);
    vRightBtn = findViewById(R.id.rightBtn);
    vRightBtn.setOnClickListener(this);
    vNoNetWork = findViewById(R.id.no_network_view);
    vRetryBtn = findViewById(R.id.network_retry_btn);
    vRetryBtn.setOnClickListener(this);
    webView = (WebView) findViewById(R.id.webView);
    bar = (ProgressBar) findViewById(R.id.myProgressBar);
    bar.setMax(100);
    webView.getSettings().setJavaScriptEnabled(true);
    webView.getSettings().setSupportZoom(false);
    webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
    webView.getSettings().setSupportMultipleWindows(true);
    // 开启 DOM storage API 功能
    webView.getSettings().setDomStorageEnabled(true);
    //开启 database storage API 功能
    webView.getSettings().setDatabaseEnabled(true);
    //webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
    String cacheDirPath = getFilesDir().getAbsolutePath() + APP_CACAHE_DIRNAME;
    //设置数据库缓存路径
    webView.getSettings().setDatabasePath(cacheDirPath);
    //设置  Application Caches 缓存目录
    webView.getSettings().setAppCachePath(cacheDirPath);
    //开启 Application Caches 功能
    webView.getSettings().setAppCacheEnabled(true);
    webView.clearHistory();
    webView.clearCache(true);
    webView.setWebViewClient(new WebViewClient() {
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url.contains("m_login")) {
          logined = false;
          doAutoLogin();
          return true;
        }
        if (!url.contains("m_enter")) {
          if (vTopBar.getVisibility() == View.GONE) {
            vTopBar.setVisibility(View.VISIBLE);
          }
        }
        lastUrl = url;
        webView.loadUrl(url);
        return true;
      }

      @Override
      public void onPageStarted(WebView view, String url, Bitmap favicon) {
        if (url.contains("m_enter") && !hasLoaded) {
          webView.clearHistory();
          vLeftBtn.setVisibility(View.GONE);
          return;
        }
        if (url.contains("m_enter")) {
          hasLoaded = true;
        }
        bar.setVisibility(View.VISIBLE);
        super.onPageStarted(view, url, favicon);
      }

      @Override
      public void onPageFinished(WebView view, String url) {
        bar.setVisibility(View.GONE);
        if (url.contains("m_enter") || url.contains("m_hall") || url.contains("circle-homepage")
            || url.contains("m_rankhome") || url.contains("m_shopping")
            || url.contains("m_show-competition")) {
          vLeftBtn.setVisibility(View.GONE);
          webView.clearHistory();
        } else {
          if (webView.canGoBack()) {
            vLeftBtn.setVisibility(View.VISIBLE);
          } else {
            vLeftBtn.setVisibility(View.GONE);
          }
        }
        vTitleTxt.setText(view.getTitle());
        super.onPageFinished(view, url);
      }

      @Override
      public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        handler.proceed();
      }

      @Override
      public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        lastUrl = failingUrl;
        onErrorPage();
        if (!CommonUtil.isNetWorkConnected(MainActivity.this)) {
          showSimpleMessageDialog("网络无法连接，请检查网络");
        } else {
          showSimpleMessageDialog("加载失败了，请稍后重试");
        }
      }
    });
    webView.setWebChromeClient(new WebChromeClient() {
      @Override
      public void onProgressChanged(WebView view, int newProgress) {
        bar.setProgress(newProgress);
        if (newProgress == 100) {
          bar.setVisibility(View.GONE);
        }
        super.onProgressChanged(view, newProgress);
      }

      @Override
      public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);
        vTitleTxt.setText(title);
      }

      @Override
      public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
        new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_HOLO_LIGHT).setTitle(null).setPositiveButton("确定", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            result.confirm();
          }
        }).setMessage(message).show();
        return true;
      }

      @Override
      public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        mUploadMessageForAndroid5 = filePathCallback;
        showSelector();
        return true;
      }

      public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        mUploadMessage = uploadMsg;
        showSelector();
      }

      //3.0--版本
      public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        mUploadMessage = uploadMsg;
        showSelector();
      }

      public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        mUploadMessage = uploadMsg;
        showSelector();
      }
    });
    webView.loadUrl(Constants.BASE_URL);
  }

  private void doAutoLogin() {
    acc = dbHelper.getValue(BeanPropEnum.AppProp.acc.toString());
    pwd = dbHelper.getValue(BeanPropEnum.AppProp.pwd.toString());
    if (!CommonUtil.isEmpty(acc) && !CommonUtil.isEmpty(pwd) && !loginedOnce) {
      //doLogin();
      switchTo(LoginActivity.class);
    } else {
      switchTo(LoginActivity.class);
    }
  }

  private void doLogin() {
    if (progressDialog == null) {
      progressDialog = new CustomProgressDialog(this, "正在登录...", false);
    }
    progressDialog.setMsg("正在登录...");
    progressDialog.show();
    List<NameValuePair> paramspost = new ArrayList<NameValuePair>();
    paramspost.add(new BasicNameValuePair("loginName_login", acc));
    paramspost.add(new BasicNameValuePair("passWord_login", pwd));
    networkHandler.post(Constants.LOGIN_URL, paramspost, 15, new Callback<TransResp>() {
      @Override
      public void callback(TransResp transResp) {
        if (transResp.getRetcode() == 200) {
          progressDialog.dismiss();
          if ("0".equals(transResp.getRetjson())) {
            loadMain();
            return;
          } else {
            Toast.makeText(MainActivity.this, "登录失败了", Toast.LENGTH_SHORT).show();
            switchTo(LoginActivity.class);
          }
        } else {
          progressDialog.dismiss();
          Toast.makeText(MainActivity.this, "登录失败了", Toast.LENGTH_SHORT).show();
          switchTo(LoginActivity.class);
          return;
        }
      }
    });

  }

  private long exitTime = 0;

  public void backTo() {
    if ((System.currentTimeMillis() - exitTime) > 2000) {
      Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
      exitTime = System.currentTimeMillis();
    } else {
      this.finish();
      System.exit(0);
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
      if (actionSheet != null && isShowing) {
        actionSheet1.dismiss();
        return true;
      }
      if (needBackHome) {
        webView.loadUrl(Constants.HOME_URL);
        needBackHome = false;
        return true;
      }
      if (webView.canGoBack()) {
        webView.goBack();
        return true;
      } else {
        vLeftBtn.setVisibility(View.GONE);
      }
      backTo();
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  private void showSelector() {
    if (actionSheet == null) {
      actionSheet = ActionSheet.createBuilder(this, getSupportFragmentManager())
          .setCancelButtonTitle("取消")
          .setOtherButtonTitles("相册", "拍照")
          .setCancelableOnTouchOutside(true)
          .setListener(this);
      tempFile = FileUtil.getImageFile("photo.jpg");
    }
    actionSheet1 = actionSheet.show();
    isShowing = true;
  }

  @Override
  public void onDismiss(ActionSheet actionSheet, boolean isCancel) {
    if (isCancel) {
      realseFileChooser();
    }
    isShowing = false;
  }

  @Override
  public void onOtherButtonClick(ActionSheet actionSheet, int i) {
    if (i == 0) {
      fromGallery();
    } else {
      takePhoto();
    }
  }

  private void realseFileChooser() {
    if (uri != null) {
      if (mUploadMessageForAndroid5 != null) {
        mUploadMessageForAndroid5.onReceiveValue(new Uri[]{uri});
      }
      if (mUploadMessage != null) {
        mUploadMessage.onReceiveValue(uri);
      }
      uri = null;
    } else {
      if (mUploadMessageForAndroid5 != null) {
        mUploadMessageForAndroid5.onReceiveValue(new Uri[]{});
      }
      if (mUploadMessage != null) {
        mUploadMessage.onReceiveValue(null);
      }
    }
    //FileUtil.delImageFile(tempFileName);
    mUploadMessage = null;
    mUploadMessageForAndroid5 = null;
  }


  private Intent intentFromGallery;

  private void fromGallery() {
    if (intentFromGallery == null) {
      intentFromGallery = new Intent(Intent.ACTION_PICK, null);
      intentFromGallery.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
    }
    startActivityForResult(intentFromGallery, PHOTO_REQUEST_CODE);
  }

  private void takePhoto() {
    if (tempFile == null) {
      realseFileChooser();
      showSimpleMessageDialog("无法在SD卡上生成图片");
      return;
    }
    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
    startActivityForResult(intent, CAMERA_REQUEST_CODE);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      // 如果是直接从相册获取
      case PHOTO_REQUEST_CODE:
        if (data != null && data.getData() != null) {
          uri = data.getData();
          startPhotoZoom(uri);
        } else {
          realseFileChooser();
        }
        break;
      // 如果是调用相机拍照时
      case CAMERA_REQUEST_CODE:
        if (tempFile == null || !tempFile.exists()) {
          realseFileChooser();
          return;
        }
        uri = Uri.fromFile(tempFile);
        if (uri == null) {
          realseFileChooser();
          return;
        }
        startPhotoZoom(uri);
        break;
      // 取得裁剪后的图片
      case RESULT_REQUEST_CODE:
        if (data != null) {
          uri = Uri.fromFile(tempFile);
          realseFileChooser();
        } else {
          realseFileChooser();
        }
        break;
      default:
        break;
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  public void startPhotoZoom(Uri uri) {
    Intent intent = new Intent("com.android.camera.action.CROP");
    intent.setDataAndType(uri, "image/*");
    // 设置裁剪
    intent.putExtra("crop", "true");
    // aspectX aspectY 是宽高的比例
    intent.putExtra("aspectX", 3);
    intent.putExtra("aspectY", 3);
    // outputX outputY 是裁剪图片宽高
    intent.putExtra("outputX", 300);
    intent.putExtra("outputY", 300);
    intent.putExtra("return-data", false);
    intent.putExtra("scaleUpIfNeeded", true);
    intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
    intent.putExtra("scale", true);
    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
    startActivityForResult(intent, RESULT_REQUEST_CODE);
  }

  public static Handler mHandler;

  private void initOther() {
    mHandler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
        if (msg.what == 100) {
          if (webView != null) {
            webView.clearCache(true);
            webView.clearFormData();
          }
        } else if (msg.what == 101) {
          //注册
          webView.loadUrl(Constants.REG_URL);
        } else if (msg.what == 102) {
          //忘记密码
          webView.loadUrl(Constants.PWD_URL);
        } else if (msg.what == 103) {
          //大厅
          loginedOnce = true;
          logined = true;
          String acc = (String) msg.obj;
          setAlias(acc);
          loadMain();
        } else if (msg.what == 104) {
          lastUrl = Constants.MSG_URL;
          needBackHome = true;
          if (!logined) {
            loginedOnce = false;
            doAutoLogin();
          } else {
            webView.loadUrl(lastUrl);
          }
        }
        super.handleMessage(msg);
      }
    };
  }

  private void loadMain() {
    CookieSyncManager.createInstance(MainActivity.this);
    CookieManager cookieManager = CookieManager.getInstance();
    Cookie sessionCookie = Constants.LOGINCOOKIE;
    if (sessionCookie != null) {
      String cookieString = sessionCookie.getName() + "="
          + sessionCookie.getValue() + "; domain="
          + sessionCookie.getDomain();
      cookieManager.setCookie(Constants.HOME_URL, cookieString);
      CookieSyncManager.getInstance().sync();
    }
    if (!CommonUtil.isEmpty(lastUrl)) {
      webView.loadUrl(lastUrl);
    } else {
      webView.loadUrl(Constants.HOME_URL);
    }
  }

  @Override
  public void onClick(View v) {
    if (v == vLeftBtn) {
      if (webView.canGoBack()) {
        webView.goBack();
      } else {
        vLeftBtn.setVisibility(View.GONE);
      }
    } else if (v == vRightBtn) {
      switchTo(SettingActivity.class);
    } else if (v == vRetryBtn) {
      webView.loadUrl(lastUrl);
      webView.setVisibility(View.VISIBLE);
      vNoNetWork.setVisibility(View.GONE);
    }
  }

  private void onErrorPage() {
    webView.setVisibility(View.GONE);
    vNoNetWork.setVisibility(View.VISIBLE);
  }

  //for receive customer msg from jpush server
  private MessageReceiver mMessageReceiver;
  public static final String MESSAGE_RECEIVED_ACTION = "com.example.jpushdemo.MESSAGE_RECEIVED_ACTION";
  public static final String KEY_TITLE = "title";
  public static final String KEY_MESSAGE = "message";
  public static final String KEY_EXTRAS = "extras";

  public void registerMessageReceiver() {
    mMessageReceiver = new MessageReceiver();
    IntentFilter filter = new IntentFilter();
    filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
    filter.addAction(MESSAGE_RECEIVED_ACTION);
    registerReceiver(mMessageReceiver, filter);
  }

  public class MessageReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      if (MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
        String messge = intent.getStringExtra(KEY_MESSAGE);
        String extras = intent.getStringExtra(KEY_EXTRAS);
        StringBuilder showMsg = new StringBuilder();
        showMsg.append(KEY_MESSAGE + " : " + messge + "\n");
        if (!CommonUtil.isEmpty(extras)) {
          showMsg.append(KEY_EXTRAS + " : " + extras + "\n");
        }
        setCostomMsg(showMsg.toString());
      }
    }
  }

  private void setAlias(String userid) {

    // 调用 Handler 来异步设置别名
    pushHandler.sendMessage(pushHandler.obtainMessage(MSG_SET_ALIAS, userid));
  }

  private final TagAliasCallback mAliasCallback = new TagAliasCallback() {
    @Override
    public void gotResult(int code, String alias, Set<String> tags) {
      String logs;
      switch (code) {
        case 0:
          logs = "Set tag and alias success";
          // 建议这里往 SharePreference 里写一个成功设置的状态。成功设置一次后，以后不必再次设置了。
          break;
        case 6002:
          logs = "Failed to set alias and tags due to timeout. Try again after 60s.";
          // 延迟 60 秒来调用 Handler 设置别名
          pushHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SET_ALIAS, alias), 1000 * 60);
          break;
        default:
          logs = "Failed with errorCode = " + code;
      }
    }
  };
  private static final int MSG_SET_ALIAS = 1001;
  private final Handler pushHandler = new Handler() {
    @Override
    public void handleMessage(android.os.Message msg) {
      super.handleMessage(msg);
      switch (msg.what) {
        case MSG_SET_ALIAS:
          // 调用 JPush 接口来设置别名。
          JPushInterface.setAliasAndTags(getApplicationContext(),
              (String) msg.obj,
              null,
              mAliasCallback);
          break;
        default:
      }
    }
  };

  private void setCostomMsg(String msg) {

  }

  @Override
  protected void onDestroy() {
    unregisterReceiver(mMessageReceiver);
    super.onDestroy();
  }
}
