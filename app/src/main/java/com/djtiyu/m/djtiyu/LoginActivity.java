package com.djtiyu.m.djtiyu;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.djtiyu.m.djtiyu.db.BeanPropEnum;
import com.djtiyu.m.djtiyu.db.MsgCodeBean;
import com.djtiyu.m.djtiyu.db.QQRetBean;
import com.djtiyu.m.djtiyu.db.QQUserInfor;
import com.djtiyu.m.djtiyu.util.Callback;
import com.djtiyu.m.djtiyu.util.CommonUtil;
import com.djtiyu.m.djtiyu.util.Constants;
import com.djtiyu.m.djtiyu.util.CustomProgressDialog;
import com.djtiyu.m.djtiyu.util.TransResp;
import com.google.gson.Gson;
import com.umeng.socialize.Config;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by shuwei on 16/9/13.
 */
public class LoginActivity extends BaseActivity {
  private View vGoaway, vRegisterBtn, vForgetBtn, vLoginBtn, vLoginByWechat, vLoginByQQ, vLoginBySina;
  private EditText vAcc, vPwd;
  private String acc, pwd;
  private UMShareAPI mShareAPI = null;
  private Gson gson = new Gson();


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    setPlatform();
    initView();
    authPer();
  }

  private void authPer() {
    if (Build.VERSION.SDK_INT >= 23) {
      String[] mPermissionList = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.CALL_PHONE, android.Manifest.permission.READ_LOGS, android.Manifest.permission.READ_PHONE_STATE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.SET_DEBUG_APP, android.Manifest.permission.SYSTEM_ALERT_WINDOW, android.Manifest.permission.GET_ACCOUNTS, android.Manifest.permission.WRITE_APN_SETTINGS};
      //TODO
    }
  }

  private void setPlatform() {
    //微信
    PlatformConfig.setWeixin(Constants.WECHAT_APPID, Constants.WECHAT_APPKEY);
    //新浪微博
    PlatformConfig.setSinaWeibo(Constants.SINA_APPID, Constants.SINA_APPKEY);
    // qq qzone appid appkey
    PlatformConfig.setQQZone(Constants.QQ_APPID, Constants.QQ_APPKEY);
    mShareAPI = UMShareAPI.get(this);
    Config.REDIRECT_URL="http://sns.whalecloud.com/sina2/callback";
  }

  private void initView() {
    vGoaway = findViewById(R.id.goaway);
    vGoaway.setOnClickListener(this);

    vRegisterBtn = findViewById(R.id.login_regist);
    vRegisterBtn.setOnClickListener(this);

    vForgetBtn = findViewById(R.id.forget_pwd);
    vForgetBtn.setOnClickListener(this);

    vLoginBtn = findViewById(R.id.sign_in_button);
    vLoginBtn.setOnClickListener(this);

    vLoginByWechat = findViewById(R.id.loginByWechat);
    vLoginByWechat.setOnClickListener(this);

    vLoginByQQ = findViewById(R.id.loginByQQ);
    vLoginByQQ.setOnClickListener(this);

    vLoginBySina = findViewById(R.id.loginBySina);
    vLoginBySina.setOnClickListener(this);

    vAcc = (EditText) findViewById(R.id.login_user);
    vPwd = (EditText) findViewById(R.id.login_pwd);
    initViewData();
  }

  private void initViewData() {
    acc = dbHelper.getValue(BeanPropEnum.AppProp.acc.toString());
    pwd = dbHelper.getValue(BeanPropEnum.AppProp.pwd.toString());
    if (!CommonUtil.isEmpty(acc) && !CommonUtil.isEmpty(pwd)) {
      vAcc.setText(acc);
      vPwd.setText(pwd);
      vAcc.setSelection(acc.length());
      vPwd.setSelection(pwd.length());
    }

  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.login_regist:
        Message message = new Message();
        message.what = 101;
        MainActivity.mHandler.sendMessage(message);
        this.finish();
        break;
      case R.id.forget_pwd:
        Message msg = new Message();
        msg.what = 102;
        MainActivity.mHandler.sendMessage(msg);
        this.finish();
        break;
      case R.id.sign_in_button:
        doLogin();
        break;
      case R.id.goaway:
        Message pas = new Message();
        pas.what = 105;
        MainActivity.mHandler.sendMessage(pas);
        this.finish();
        break;
      default:
        onClickAuth(v);
        break;
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
      Message pas = new Message();
      pas.what = 106;
      MainActivity.mHandler.sendMessage(pas);
      this.finish();
    }
    return super.onKeyDown(keyCode, event);
  }

  private void doLogin() {
    acc = vAcc.getText().toString();
    pwd = vPwd.getText().toString();
    if (CommonUtil.isEmpty(acc)) {
      showSimpleMessageDialog("请输入手机号");
      return;
    }
    if (CommonUtil.isEmpty(pwd)) {
      showSimpleMessageDialog("请输入密码");
      return;
    }
    if (progressDialog == null) {
      progressDialog = new CustomProgressDialog(this, "正在登录...", false);
    }
    progressDialog.setMsg("正在登录...");
    progressDialog.show();
    //acc = "18217530165";
    //pwd = "123456";
    List<NameValuePair> paramspost = new ArrayList<NameValuePair>();
    paramspost.add(new BasicNameValuePair("loginName_login", acc));
    paramspost.add(new BasicNameValuePair("passWord_login", pwd));
    networkHandler.post(Constants.LOGIN_URL, paramspost, 15, new Callback<TransResp>() {
      @Override
      public void callback(TransResp transResp) {
        if (transResp.getRetcode() == 200) {
          progressDialog.dismiss();
          if ("0".equals(transResp.getRetjson())) {
            dbHelper.saveOrUpdateKeyValue(BeanPropEnum.AppProp.acc.toString(), acc);
            dbHelper.saveOrUpdateKeyValue(BeanPropEnum.AppProp.pwd.toString(), pwd);
            Message msg = new Message();
            msg.what = 103;
            msg.obj = acc;
            MainActivity.mHandler.sendMessage(msg);
            LoginActivity.this.finish();
            return;
          } else if ("1".equals(transResp.getRetjson())) {
            showSimpleMessageDialog("用户不存在");
          } else if ("2".equals(transResp.getRetjson())) {
            showSimpleMessageDialog("密码错误");
          } else {
            showSimpleMessageDialog("登录失败");
          }
        } else {
          progressDialog.dismiss();
          showSimpleMessageDialog("登录失败了");
          return;
        }
      }
    });

  }

  public void onClickAuth(View view) {
    SHARE_MEDIA platform = null;
    if (view.getId() == R.id.loginBySina) {
      platform = SHARE_MEDIA.SINA;
    } else if (view.getId() == R.id.loginByQQ) {
      platform = SHARE_MEDIA.QQ;
    } else if (view.getId() == R.id.loginByWechat) {
      platform = SHARE_MEDIA.WEIXIN;
      //mShareAPI.deleteOauth(LoginActivity.this,platform,umdelAuthListener);
    } else {
      return;
    }
//    if(progressDialog==null){
//      progressDialog = new CustomProgressDialog(this,"正在加载",false);
//    }
    //progressDialog.setMsg("正在加载");
    mShareAPI.doOauthVerify(LoginActivity.this, platform, umAuthListener);
  }

  /**
   * auth callback interface
   **/
  private UMAuthListener umAuthListener = new UMAuthListener() {
    @Override
    public void onComplete(SHARE_MEDIA platform, int action, Map<String, String> data) {
      //Toast.makeText(getApplicationContext(), "Authorize succeed", Toast.LENGTH_SHORT).show();
      Log.d("user info", "user info:" + data.toString());
      //showSimpleMessageDialog(data.toString());
      getUserInfor(platform, data);
    }

    @Override
    public void onError(SHARE_MEDIA platform, int action, Throwable t) {
      Toast.makeText(getApplicationContext(), "授权失败了", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancel(SHARE_MEDIA platform, int action) {
      Toast.makeText(getApplicationContext(), "未授权", Toast.LENGTH_SHORT).show();
    }
  };
  /**
   * delauth callback interface
   **/
  private UMAuthListener umdelAuthListener = new UMAuthListener() {
    @Override
    public void onComplete(SHARE_MEDIA platform, int action, Map<String, String> data) {
      Toast.makeText(getApplicationContext(), "delete Authorize succeed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(SHARE_MEDIA platform, int action, Throwable t) {
      Toast.makeText(getApplicationContext(), "delete Authorize fail", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancel(SHARE_MEDIA platform, int action) {
      Toast.makeText(getApplicationContext(), "delete Authorize cancel", Toast.LENGTH_SHORT).show();
    }
  };

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    mShareAPI.onActivityResult(requestCode, resultCode, data);
  }

  private int method;

  private void getUserInfor(SHARE_MEDIA platform, Map<String, String> data) {
    if (progressDialog == null) {
      progressDialog = new CustomProgressDialog(this, "正在授权...", false);
    }
    progressDialog.setMsg("正在授权...");
    progressDialog.show();
    String url = "";
    final QQRetBean qqRetBean = new QQRetBean();
    if (platform == SHARE_MEDIA.QQ) {
        qqRetBean.setAccess_token(data.get("access_token"));
        qqRetBean.setAppid(Constants.QQ_APPID);
        qqRetBean.setOpenid(data.get("openid"));
        qqRetBean.setExpires_in(data.get("expires_in"));
        url = "https://graph.qq.com/user/get_user_info?access_token=" + qqRetBean.getAccess_token() + "&oauth_consumer_key=" + Constants.QQ_APPID + "&openid=" + qqRetBean.getOpenid();
        method = 1;
    } else if (platform == SHARE_MEDIA.SINA) {
      qqRetBean.setAccess_token(data.get("access_key"));
      qqRetBean.setAppid(Constants.SINA_APPID);
      qqRetBean.setUid(data.get("uid"));
      method = 2;
      url="https://api.weibo.com/2/users/show.json?access_token="+qqRetBean.getAccess_token()+"&uid="+qqRetBean.getUid();
    } else if (platform == SHARE_MEDIA.WEIXIN) {
      method = 3;
      qqRetBean.setAccess_token(data.get("access_token"));
      qqRetBean.setOpenid(data.get("openid"));
      qqRetBean.setUnionid(data.get("unionid"));
      url = "https://api.weixin.qq.com/sns/userinfo?access_token=" + qqRetBean.getAccess_token() + "&openid=" + qqRetBean.getOpenid();
    }

    networkHandler.get(url, null, 30, new Callback<TransResp>() {
          @Override
          public void callback(TransResp transResp) {
            if (transResp.getRetcode() == HttpStatus.SC_OK) {
              try {
                QQUserInfor userInfor = gson.fromJson(transResp.getRetjson(), QQUserInfor.class);
                userInfor.setAccess_token(qqRetBean.getAccess_token());
                userInfor.setOpenid(qqRetBean.getOpenid());
                if (method == 1) {
                  userInfor.setLogintype(Constants.LOGIN_QQ);
                } else if (method == 3) {
                  userInfor.setLogintype(Constants.LOGIN_WECHAT);
                } else {
                  userInfor.setOpenid(qqRetBean.getUid());
                  userInfor.setLogintype(Constants.LOGIN_SINA);
                }
                doSendToServer(userInfor);
              } catch (Exception e) {
                progressDialog.dismiss();
                showSimpleMessageDialog("授权失败了");
              }
            } else {
              progressDialog.dismiss();
              showSimpleMessageDialog("授权失败了");
            }
          }
        }
    );
  }

  private void doSendToServer(final QQUserInfor paramspost) {
    String json = gson.toJson(paramspost);

    networkHandler.postJson(Constants.LOGIN_AUTH_URL, json, 30, new Callback<TransResp>() {
      @Override
      public void callback(TransResp transResp) {
        if (transResp.getRetcode() == HttpStatus.SC_OK) {
          progressDialog.dismiss();
          MsgCodeBean bean = gson.fromJson(transResp.getRetjson(), MsgCodeBean.class);
          if (bean != null && bean.getStatus().equals("success")) {
            dbHelper.saveOrUpdateKeyValue(BeanPropEnum.AppProp.openid.toString(), paramspost.getOpenid());
            dbHelper.saveOrUpdateKeyValue(BeanPropEnum.AppProp.access_token.toString(), paramspost.getAccess_token());
            dbHelper.saveOrUpdateKeyValue(BeanPropEnum.AppProp.logintype.toString(), paramspost.getLogintype());
            Message msg = new Message();
            msg.what = 103;
            msg.obj = acc;
            MainActivity.mHandler.sendMessage(msg);
            LoginActivity.this.finish();
            return;
          } else {
            showSimpleMessageDialog(transResp.getRetjson());
          }
          //
        } else {
          progressDialog.dismiss();
          showSimpleMessageDialog("授权失败了" + transResp.getRetcode());
        }
      }
    });
  }
}
