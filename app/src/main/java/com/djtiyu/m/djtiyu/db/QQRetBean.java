package com.djtiyu.m.djtiyu.db;

/**
 * Created by shuwei on 16/9/23.
 */
public class QQRetBean {
  private String access_token;
  private String page_type;
  private String appid;
  private String pfkey;
  private String uid;
  private String auth_time;
  private String sendinstall;
  private String pf;
  private String expires_in;
  private String pay_token;
  private String ret;
  private String openid;
  private String unionid;


  public String getAccess_token() {
    return access_token;
  }

  public void setAccess_token(String access_token) {
    this.access_token = access_token;
  }

  public String getAppid() {
    return appid;
  }

  public void setAppid(String appid) {
    this.appid = appid;
  }

  public String getAuth_time() {
    return auth_time;
  }

  public void setAuth_time(String auth_time) {
    this.auth_time = auth_time;
  }

  public String getExpires_in() {
    return expires_in;
  }

  public void setExpires_in(String expires_in) {
    this.expires_in = expires_in;
  }

  public String getOpenid() {
    return openid;
  }

  public void setOpenid(String openid) {
    this.openid = openid;
  }

  public String getPage_type() {
    return page_type;
  }

  public void setPage_type(String page_type) {
    this.page_type = page_type;
  }

  public String getPay_token() {
    return pay_token;
  }

  public void setPay_token(String pay_token) {
    this.pay_token = pay_token;
  }

  public String getPf() {
    return pf;
  }

  public void setPf(String pf) {
    this.pf = pf;
  }

  public String getPfkey() {
    return pfkey;
  }

  public void setPfkey(String pfkey) {
    this.pfkey = pfkey;
  }

  public String getRet() {
    return ret;
  }

  public void setRet(String ret) {
    this.ret = ret;
  }

  public String getSendinstall() {
    return sendinstall;
  }

  public void setSendinstall(String sendinstall) {
    this.sendinstall = sendinstall;
  }

  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public String getUnionid() {
    return unionid;
  }

  public void setUnionid(String unionid) {
    this.unionid = unionid;
  }
}
