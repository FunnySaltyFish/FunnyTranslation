package com.funny.translation.utils;

import android.util.Log;

import java.io.IOException;
import java.util.Map;

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
}
