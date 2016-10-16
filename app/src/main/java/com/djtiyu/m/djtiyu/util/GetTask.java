package com.djtiyu.m.djtiyu.util;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;


public class GetTask extends AsyncTask<String, Long, TransResp> {

    private final String url;
    private HttpParams queryParams;
    private int timeout;
    private final Callback<TransResp> callback;

    GetTask(String url, HttpParams queryParams, int timeout, Callback<TransResp> callback) {
        this.url = url;
        this.timeout = timeout;
        this.queryParams = queryParams;
        this.callback = callback;
    }

    @Override
    protected TransResp doInBackground(String... params) {

        /*TransResp resp = new TransResp();
        try {
            *//*final Client client = Client.create();
            client.setConnectTimeout(timeout < 10 ? 10 * 1000 : timeout * 1000);

            final WebResource resource = client.resource(url);
            if (queryParams != null) {
                resource.queryParams(queryParams);
            }*//*
            String result = null;
            HttpGet httpGet = new HttpGet(url);
            HttpClient httpClient = HttpUtil.getHttpsClient(url,timeout*1000);
            if(queryParams!=null){
                httpGet.setParams(queryParams);
            }
            //httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("Accept", "application/json");
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                result = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
                resp.setRetcode(httpResponse.getStatusLine().getStatusCode());
                resp.setRetjson(result);
            } else {
                resp.setRetcode(httpResponse.getStatusLine().getStatusCode());
                result = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
                resp.setRetmsg(result);
            }
            return resp;
        } catch (Exception e) {
            resp.setRetcode(HttpStatus.SC_NOT_FOUND);
            String retmsg = "";
            resp.setRetmsg(retmsg);
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
        uRLConnection.setRequestMethod("GET");
        uRLConnection.setUseCaches(false);
        uRLConnection.setRequestProperty("Connection", "Keep-Alive");
        uRLConnection.setConnectTimeout(this.timeout * 1000);
        uRLConnection.setInstanceFollowRedirects(false);
        uRLConnection.setRequestProperty("Content-Type",  "application/x-www-form-urlencoded");
        uRLConnection.setReadTimeout(this.timeout * 1000);
        uRLConnection.setDoOutput(false);
        uRLConnection.connect();

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
