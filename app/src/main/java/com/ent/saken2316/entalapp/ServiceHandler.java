package com.ent.saken2316.entalapp;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class ServiceHandler {
    static String response;
    public final static int SIGNIN = 0;
    public final static int SIGNUP = 1;
    public final static int GET = 2;
    public final static int POST = 3;

    public String[] makeServiceCall(String url, int method) {
        return this.makeServiceCall(url, method, null, null, null);
    }
    public ServiceHandler() {

    }
    public String[] makeServiceCall(String url, int method,
                                    List<NameValuePair> params, String csrftoken, String sessionid) {

        try {
            Log.e("TEMIRULAN'S LOG", "[" + url + "] " + "[" + Integer.toString(method) + "] ");
        } catch (Exception e) {
            Log.e("TEMIRULAN", e.toString());
        }
        String[] arrayListResponse = new String[3];

        try {
            // http client
            HttpParams httpParams = new BasicHttpParams();
            httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            if (method == POST || method == GET){

                HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
                HttpConnectionParams.setSoTimeout(httpParams, 5000);
            }
            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpEntity httpEntity = null;
            HttpResponse httpResponse = null;

            // Checking http request method type
            if (method == SIGNIN){

                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(new UrlEncodedFormEntity(params));
                CookieStore cookieStore = new BasicCookieStore();
                HttpContext httpContext = new BasicHttpContext();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

                httpResponse = httpClient.execute(httpPost, httpContext);
                List<Cookie> cookies = cookieStore.getCookies();
                Log.e("Cookie store", cookieStore.toString());

                if (!cookies.isEmpty()){
                    arrayListResponse[0] = cookies.get(0).getValue();
                    arrayListResponse[1] = cookies.get(1).getValue();
                }

            } else if (method == SIGNUP) {

                Log.e("Params", params.toString());
                Log.e("First name params", params.get(2).getValue());
                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(new UrlEncodedFormEntity(params, "utf-8"));

                CookieStore cookieStore = new BasicCookieStore();
                HttpContext httpContext = new BasicHttpContext();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

                httpResponse = httpClient.execute(httpPost, httpContext);
                List<Cookie> cookies = cookieStore.getCookies();
                Log.e("Cookie store", cookieStore.toString());

                if (!cookies.isEmpty()){
                    arrayListResponse[0] = cookies.get(0).getValue();
                    arrayListResponse[1] = cookies.get(1).getValue();
                }

            } else if (method == POST) {

                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(new UrlEncodedFormEntity(params, "utf-8"));

                String header = "csrftoken=" + csrftoken + "; sessionid=" + sessionid + ";";
                httpPost.addHeader("cookie", header);

                Log.e("Hello", "Tima");
                httpResponse = httpClient.execute(httpPost);
                Log.e("Hello", "Saken");

            } else if (method == GET) {

                if (params != null) {
                    String paramString = URLEncodedUtils
                            .format(params, "utf-8");
                    url += "?" + paramString;
                }
                HttpGet httpGet = new HttpGet(url);

                String header = "csrftoken=" + csrftoken + "; sessionid=" + sessionid + ";";
                httpGet.addHeader("cookie", header);
                Log.e("Header", header);

                Log.e("Hello", "Tima");
                httpResponse = httpClient.execute(httpGet);
                Log.e("Hello", "Saken");
            }
            httpEntity = httpResponse.getEntity();
            response = EntityUtils.toString(httpEntity);
            Log.e("Entity response", response);
            arrayListResponse[2] = response;

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return arrayListResponse;
    }
}






