package com.djtiyu.m.djtiyu;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.djtiyu.m.djtiyu.util.CustomProgressDialog;
import com.djtiyu.m.djtiyu.util.NetworkHandler;

/**
 * Created by shuwei on 16/9/13.
 */
public class BaseActivity extends AppCompatActivity implements View.OnClickListener {
  protected NetworkHandler networkHandler;
  protected CustomProgressDialog progressDialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if(networkHandler==null){
      networkHandler = NetworkHandler.getInstance();
    }
  }

  protected void showSimpleMessageDialog(String msg) {
    if (isFinishing()) {
      return;
    }
    if ("请登录".equals(msg)) {
      switchTo(LoginActivity.class);
      return;
    }
    new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT).setTitle(null).setNegativeButton("确定", null).setMessage(msg).show();
  }

  @Override
  public void onClick(View v) {

  }

  protected void switchTo(Class clazz) {
    Intent intent = new Intent(this, clazz);
    startActivity(intent);
  }
}
