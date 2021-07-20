package com.funny.translation.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.JavaNetCookieJar;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpUtil {
    private static OkHttpClient okHttpClient;
    private static final Object lock = new Object();
    public static OkHttpClient getClient(){
        if (okHttpClient==null){
            synchronized (lock){
                init();
            }
        }
        return okHttpClient;
    }

    public static void init(){
        okHttpClient = new OkHttpClient.Builder()
                .readTimeout(15,TimeUnit.SECONDS)
                .connectTimeout(5,TimeUnit.SECONDS)
                .build();
    }

    public static String get(String url) throws IOException{
        OkHttpClient client = getClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = null;
        response = client.newCall(request).execute();
        String string = response.body().string();
        return string;
        //Log.i(TAG, "获取到的string:" + string);
    }

    public static byte[] getRaw(String url) throws IOException{
        OkHttpClient client = getClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = null;
        response = client.newCall(request).execute();
        return Objects.requireNonNull(response.body()).bytes();
        //Log.i(TAG, "获取到的string:" + string);
    }

    public static InputStream getInputStream(String url) throws IOException{
        OkHttpClient client = getClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = null;
        response = client.newCall(request).execute();
        return Objects.requireNonNull(response.body()).byteStream();
        //Log.i(TAG, "获取到的string:" + string);
    }

    public static String get(String url, HashMap<String,String> headers) throws IOException{
        OkHttpClient client = getClient();
        Headers.Builder headersBuilder = new Headers.Builder();

        for (HashMap.Entry<String,String> entry : headers.entrySet()){
            headersBuilder.add(entry.getKey(),entry.getValue());
        }

        Request request = new Request.Builder()
                .url(url)
                .headers(headersBuilder.build())
                .build();
        Response response = client.newCall(request).execute();
        String string = response.body().string();
        return string;
        //Log.i(TAG, "获取到的string:" + string);
    }

    public static String postJSON(String url,String json) throws IOException {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json,JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = getClient().newCall(request).execute();
        return Objects.requireNonNull(response.body()).string();
    }

    public static String postJSON(String url,String json,HashMap<String,String> headers) throws IOException {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json,JSON);


        Headers.Builder headersBuilder = new Headers.Builder();
        for (HashMap.Entry<String,String> entry : headers.entrySet()){
            headersBuilder.add(entry.getKey(),entry.getValue());
        }
        Request request = new Request.Builder()
                .url(url)
                .headers(headersBuilder.build())
                .post(body)
                .build();
        Response response = getClient().newCall(request).execute();
        return Objects.requireNonNull(response.body()).string();
    }

    public static String postForm(String url,HashMap<String,String> form) throws IOException {
        FormBody.Builder builder = new FormBody.Builder();
        for (HashMap.Entry<String,String> entry : form.entrySet()){
            builder.add(entry.getKey(),entry.getValue());
        }
        RequestBody body = builder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = getClient().newCall(request).execute();
        return Objects.requireNonNull(response.body()).string();
    }

    public static String postForm(String url,HashMap<String,String> form,HashMap<String,String> headers) throws IOException {
        FormBody.Builder builder = new FormBody.Builder();
        for (HashMap.Entry<String,String> entry : form.entrySet()){
            builder.add(entry.getKey(),entry.getValue());
        }
        RequestBody body = builder.build();
        Headers.Builder headersBuilder = new Headers.Builder();
        for (HashMap.Entry<String,String> entry : headers.entrySet()){
            headersBuilder.add(entry.getKey(),entry.getValue());
        }
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = getClient().newCall(request).execute();
        return Objects.requireNonNull(response.body()).string();
    }

    public static String getWithIP(String url,String hostName,int port) throws IOException {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //设置连接超时时间
        builder.connectTimeout( 20, TimeUnit.SECONDS);
        //设置代理,需要替换
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostName, port));
        builder.proxy(proxy);
        //cookie管理器
        CookieManager cookieManager = new CookieManager();
        OkHttpClient client = builder
                .cookieJar(new JavaNetCookieJar(cookieManager))
                .build();
        Request cookieRequest = new Request.Builder()
                .url(url)
                .get()
                .build();
        Response execute = client.newCall(cookieRequest).execute();
        return Objects.requireNonNull(execute.body()).string();
    }
}
