package com.djtiyu.m.djtiyu.util;

/**
 * Created by linqing.he on 2015/1/9.
 */

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

public class PostTask extends AsyncTask<String, String, TransResp> {

  private final String url;
  private int timeout;
  private final Callback<TransResp> callback;
  private List<NameValuePair> paramspost;

  PostTask(String url, List<NameValuePair> paramspost, int timeout, Callback<TransResp> callback) {
    this.url = url;
    this.timeout = timeout;
    this.callback = callback;
    this.paramspost = paramspost;
  }

  @Override
  protected TransResp doInBackground(String... params) {
    /*TransResp resp = new TransResp();
    HttpPost post = new HttpPost(url);
    HttpResponse httpResponse;
    try {
      if (paramspost != null) {
        post.setEntity(new UrlEncodedFormEntity(paramspost, HTTP.UTF_8));
      }
      post.setHeader("Content-Type", "application/x-www-form-urlencoded");
      post.setHeader("Accept-Encoding", "gzip,deflate");
      DefaultHttpClient httpClient = new DefaultHttpClient();
      httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, timeout*1000);
      HttpContext context = new BasicHttpContext();
      CookieStore cookieStore = new BasicCookieStore();
      context.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
      httpResponse = httpClient.execute(post, context);
      if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        resp.setRetcode(httpResponse.getStatusLine().getStatusCode());
        HttpEntity obj = httpResponse.getEntity();
        Header contentEncoding = httpResponse.getFirstHeader("Content-Encoding");
        String retjson = "";
        if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
          InputStream is = obj.getContent();
          is = new GZIPInputStream(new BufferedInputStream(is));
          InputStreamReader reader = new InputStreamReader(is, "utf-8");
          char[] data = new char[100];
          int readSize;
          StringBuffer sb = new StringBuffer();
          while ((readSize = reader.read(data)) > 0) {
            sb.append(data, 0, readSize);
          }
          retjson = sb.toString();
          reader.close();
          is.close();
        } else {
          retjson = EntityUtils.toString(obj);
        }
        resp.setRetjson(retjson);
      } else {
        resp.setRetcode(httpResponse.getStatusLine().getStatusCode());
        if (httpResponse.getEntity() != null) {
          String retmsg = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
          resp.setRetmsg(retmsg);
        }
      }
      List<Cookie> cookies = cookieStore.getCookies();
      Log.e("cookies", "cookies" + cookies.size());
      if (!cookies.isEmpty()) {
        for (int i = cookies.size(); i > 0; i--) {
          Cookie cookie = cookies.get(i - 1);
          if (cookie.getName().equalsIgnoreCase("jsessionid") && !CommonUtil.isEmpty(cookie.getValue())) {
            Constants.LOGINCOOKIE = cookie;
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return resp;*/
    URL urlReq;
    TransResp resp = new TransResp();
    HttpURLConnection uRLConnection=null;
    try {
      urlReq = new URL(url);
      if(url.startsWith("https")){
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[] { new TrustAllManager() }, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
          @Override
          public boolean verify(String arg0, SSLSession arg1) {
            return true;
          }
        });
        uRLConnection = (HttpsURLConnection) urlReq.openConnection();
      }else{
        uRLConnection = (HttpURLConnection) urlReq.openConnection();
      }
      uRLConnection.setDoInput(true);
      uRLConnection.setDoOutput(true);
      uRLConnection.setRequestMethod("POST");
      uRLConnection.setUseCaches(false);
      uRLConnection.setRequestProperty("Connection", "Keep-Alive");
      uRLConnection.setConnectTimeout(this.timeout * 1000);
      uRLConnection.setInstanceFollowRedirects(false);
      uRLConnection.setRequestProperty("Content-Type",  "application/x-www-form-urlencoded");
      uRLConnection.setReadTimeout(this.timeout * 1000);
      uRLConnection.setDoOutput(false);
      uRLConnection.connect();
      if (paramspost != null) {
        String prestr = "";
        for (int i = 0; i < paramspost.size(); i++) {
          NameValuePair nameValuePair = paramspost.get(i);
          String value = nameValuePair.getValue();
          String key = nameValuePair.getName();
          if (i == paramspost.size() - 1) {
            prestr = prestr + key + "=" +  URLEncoder.encode(value,"utf-8");
          } else {
            prestr = prestr + key + "=" + URLEncoder.encode(value,"utf-8") + "&";
          }
        }
        DataOutputStream out = new DataOutputStream(uRLConnection.getOutputStream());
        out.writeBytes(prestr);
        out.flush();
        out.close();
      }
      InputStream is = uRLConnection.getInputStream();
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      String response = "";
      String readLine = "";
      while ((readLine = br.readLine()) != null) {
        //response = br.readLine();
        response = response + readLine;
      }
      is.close();
      br.close();
      resp.setRetcode(uRLConnection.getResponseCode());
      if(uRLConnection.getResponseCode()==200){
        resp.setRetjson(response);
      }else{
        resp.setRetmsg(response);
      }
      return resp;
    }catch (ConnectTimeoutException e){
      resp.setRetcode(0);
      resp.setRetmsg("请求超时");
      return resp;
    }catch (MalformedURLException e) {
      resp.setRetcode(402);
      resp.setRetmsg("请求出现错误");
      return resp;
    } catch (IOException e) {
      e.printStackTrace();
      resp.setRetcode(402);
      resp.setRetmsg("请求出现错误");
      return resp;
    }catch (Exception e){
      e.printStackTrace();
      resp.setRetcode(402);
      resp.setRetmsg("请求出现错误");
      return resp;
    }finally {
      if(uRLConnection!=null){
        uRLConnection.disconnect();
      }
    }
  }

  @Override
  protected void onPostExecute(TransResp result) {
    callback.callback(result);
    super.onPostExecute(result);
  }
}