package com.djtiyu.m.djtiyu;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.EditText;

import com.djtiyu.m.djtiyu.util.Callback;
import com.djtiyu.m.djtiyu.util.CommonUtil;
import com.djtiyu.m.djtiyu.util.CustomProgressDialog;
import com.djtiyu.m.djtiyu.util.TransResp;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shuwei on 16/9/13.
 */
public class LoginActivity extends BaseActivity {
  private View vRegisterBtn, vForgetBtn, vLoginBtn;
  private EditText vAcc, vPwd;
  private String acc, pwd;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    initView();
  }

  private void initView() {
    vRegisterBtn = findViewById(R.id.login_regist);
    vRegisterBtn.setOnClickListener(this);

    vForgetBtn = findViewById(R.id.forget_pwd);
    vForgetBtn.setOnClickListener(this);

    vLoginBtn = findViewById(R.id.sign_in_button);
    vLoginBtn.setOnClickListener(this);

    vAcc = (EditText) findViewById(R.id.login_user);
    vPwd = (EditText) findViewById(R.id.login_pwd);
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
    progressDialog.show();
    acc = "18217530165";
    pwd = "123456";
    List<NameValuePair> paramspost = new ArrayList<NameValuePair>();
    paramspost.add(new BasicNameValuePair("loginName_login", acc));
    paramspost.add(new BasicNameValuePair("passWord_login", pwd));
    networkHandler.post("http://m.djtiyu.com/myphone/html/m_checkLogin", paramspost, 15, new Callback<TransResp>() {
      @Override
      public void callback(TransResp transResp) {
        if (transResp.getRetcode() == 200) {
          progressDialog.dismiss();
          if ("0".equals(transResp.getRetjson())) {
            Message msg = new Message();
            msg.what = 103;
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
}
