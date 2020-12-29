package com.funny.translation.utils;

import android.util.Log;

import java.io.IOException;
import java.net.CookieManager;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpUtil {
    private static OkHttpClient okHttpClient;
    public static OkHttpClient getClient(){
        if (okHttpClient==null){
            init();
        }
        return okHttpClient;
    }

    public static void init(){
        okHttpClient = new OkHttpClient.Builder().build();
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

    public static String getWithIP(String url,String hostName,int port) throws IOException {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //设置连接超时时间
        builder.connectTimeout(1, TimeUnit.MINUTES);
        //设置代理,需要替换
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostName, port));
        builder.proxy(proxy);
        //cookie管理器
        //CookieManager cookieManager = new CookieManager();
        OkHttpClient client = getClient();
        Request cookieRequest = new Request.Builder()
//                .headers(headers)
                .url(url)
                .get()
                .build();
        Response response = client.newCall(cookieRequest).execute();
        return response.body().string();
    }
}
