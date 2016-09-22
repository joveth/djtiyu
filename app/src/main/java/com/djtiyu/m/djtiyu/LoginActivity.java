package com.djtiyu.m.djtiyu;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.djtiyu.m.djtiyu.db.BeanPropEnum;
import com.djtiyu.m.djtiyu.util.Callback;
import com.djtiyu.m.djtiyu.util.CommonUtil;
import com.djtiyu.m.djtiyu.util.Constants;
import com.djtiyu.m.djtiyu.util.CustomProgressDialog;
import com.djtiyu.m.djtiyu.util.TransResp;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by shuwei on 16/9/13.
 */
public class LoginActivity extends BaseActivity {
  private View vRegisterBtn, vForgetBtn, vLoginBtn, vLoginByWechat, vLoginByQQ, vLoginBySina;
  private EditText vAcc, vPwd;
  private String acc, pwd;
  private UMShareAPI mShareAPI = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    setPlatform();
    initView();
  }

  private void setPlatform() {
    //微信
    PlatformConfig.setWeixin("wx1456b79a89aa2037", "ac0cb8672ccd67159dd7b46ac85f02ac");
    //新浪微博
    PlatformConfig.setSinaWeibo("3375114410", "2632b93d69672ca8921bb11aed215856");
    // qq qzone appid appkey
    PlatformConfig.setQQZone("100424468", "c7394704798a158208a74ab60104f0ba");
    mShareAPI = UMShareAPI.get(this);
  }

  private void initView() {
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
      default:
        onClickAuth(v);
    }
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
      Toast.makeText(getApplicationContext(), "Authorize succeed", Toast.LENGTH_SHORT).show();
      Log.d("user info", "user info:" + data.toString());
      getUserInfor(platform,data);
    }

    @Override
    public void onError(SHARE_MEDIA platform, int action, Throwable t) {
      Toast.makeText(getApplicationContext(), "Authorize fail", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancel(SHARE_MEDIA platform, int action) {
      Toast.makeText(getApplicationContext(), "Authorize cancel", Toast.LENGTH_SHORT).show();
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

  private void getUserInfor(SHARE_MEDIA platform,  Map<String, String> data){
    if (progressDialog == null) {
      progressDialog = new CustomProgressDialog(this, "正在登录...", false);
    }
    progressDialog.setMsg("正在登录...");
    progressDialog.show();




  }
}
