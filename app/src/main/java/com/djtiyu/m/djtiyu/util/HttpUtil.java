package com.djtiyu.m.djtiyu.util;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * Created by linqing.he on 2015-03-30.
 */
public class HttpUtil {

    public static HttpClient getHttpsClient(String url,int timeout) {
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeout * 1000);
        HttpConnectionParams.setSoTimeout(httpParameters, timeout * 1000);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        if (url.startsWith("https")){
            int port = 443;
            if (url.lastIndexOf(":")>-1){
                if (url.substring(url.lastIndexOf(":")).contains("/")) {
                    port = Integer.parseInt(url.substring(url.lastIndexOf(":") + 1).substring(0, url.substring(url.lastIndexOf(":")).indexOf("/") - 1));
                }else{
                    port = Integer.parseInt(url.substring(url.lastIndexOf(":") + 1));
                }
            }
            KeyStore trustStore = null;
            try {
                trustStore = KeyStore.getInstance(KeyStore
                    .getDefaultType());
                trustStore.load(null, null);
                EasySSLSocketFactory ssl = new EasySSLSocketFactory(trustStore);
                schemeRegistry.register(new Scheme("https",ssl, port));
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (UnrecoverableKeyException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
            ClientConnectionManager connManager = new ThreadSafeClientConnManager(httpParameters, schemeRegistry);
            return new DefaultHttpClient(connManager, httpParameters);
        }else{
            return new DefaultHttpClient(httpParameters);
        }



    }
}
