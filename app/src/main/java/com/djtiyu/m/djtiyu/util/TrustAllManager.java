package com.djtiyu.m.djtiyu.util;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * Created by shuwei on 16/1/14.
 */
public class TrustAllManager implements X509TrustManager {

  @Override
  public void checkClientTrusted(X509Certificate[] arg0, String arg1)
      throws CertificateException {
    // TODO Auto-generated method stub

  }

  @Override
  public void checkServerTrusted(X509Certificate[] arg0, String arg1)
      throws CertificateException {
    // TODO Auto-generated method stub

  }

  @Override
  public X509Certificate[] getAcceptedIssuers() {
    // TODO Auto-generated method stub
    return null;
  }
}