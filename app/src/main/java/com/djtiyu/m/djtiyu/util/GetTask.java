package com.djtiyu.m.djtiyu.util;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;


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

        TransResp resp = new TransResp();
        try {
            /*final Client client = Client.create();
            client.setConnectTimeout(timeout < 10 ? 10 * 1000 : timeout * 1000);

            final WebResource resource = client.resource(url);
            if (queryParams != null) {
                resource.queryParams(queryParams);
            }*/
            String result = null;
            HttpGet httpGet = new HttpGet(url);
            HttpClient httpClient = HttpUtil.getHttpsClient(url,timeout);
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

        return resp;
    }

    @Override
    protected void onPostExecute(TransResp result) {
        callback.callback(result);
        super.onPostExecute(result);
    }
}
