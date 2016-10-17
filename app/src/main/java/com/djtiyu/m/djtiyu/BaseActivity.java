package com.djtiyu.m.djtiyu;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.djtiyu.m.djtiyu.db.DBHelper;
import com.djtiyu.m.djtiyu.util.Constants;
import com.djtiyu.m.djtiyu.util.CustomProgressDialog;
import com.djtiyu.m.djtiyu.util.NetworkHandler;
import com.umeng.socialize.Config;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMShareAPI;

/**
 * Created by shuwei on 16/9/13.
 */
public class BaseActivity extends AppCompatActivity implements View.OnClickListener {
  protected NetworkHandler networkHandler;
  protected CustomProgressDialog progressDialog;
  protected DBHelper dbHelper;
  protected AlertDialog alertDialog;
  protected UMShareAPI mShareAPI = null;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if(networkHandler==null){
      networkHandler = NetworkHandler.getInstance();
    }
    if(dbHelper==null){
      dbHelper = DBHelper.getInstance(this);
    }
    //微信
    PlatformConfig.setWeixin(Constants.WECHAT_APPID, Constants.WECHAT_APPKEY);
    //新浪微博
    PlatformConfig.setSinaWeibo(Constants.SINA_APPID, Constants.SINA_APPKEY);
    // qq qzone appid appkey
    PlatformConfig.setQQZone(Constants.QQ_APPID, Constants.QQ_APPKEY);
    mShareAPI = UMShareAPI.get(this);
    Config.REDIRECT_URL="http://sns.whalecloud.com/sina2/callback";
  }

  protected void showSimpleMessageDialog(String msg) {
    if (isFinishing()) {
      return;
    }
    if ("请登录".equals(msg)) {
      switchTo(LoginActivity.class);
      return;
    }
    if(alertDialog==null){
      alertDialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT).setTitle(null).setNegativeButton("确定", null).setMessage(msg).show();
    }else{
      alertDialog.show();
    }
  }

  @Override
  public void onClick(View v) {

  }
  @Override
  protected void onDestroy() {
    if (alertDialog != null && alertDialog.isShowing()) {
      alertDialog.dismiss();
    }

    super.onDestroy();
  }

  protected void switchTo(Class clazz) {
    Intent intent = new Intent(this, clazz);
    startActivity(intent);
  }
}
