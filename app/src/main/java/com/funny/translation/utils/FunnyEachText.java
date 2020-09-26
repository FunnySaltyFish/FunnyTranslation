package com.funny.translation.utils;

import android.content.Context;

import com.funny.translation.FunnyApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FunnyEachText {
    static JSONObject WORDS;
    public static void init(Context context) throws IOException, JSONException {
        String assetsData = FileUtil.getAssetsData(context, "words.json");
        WORDS = new JSONObject(new String(assetsData.getBytes(), StandardCharsets.UTF_8));
    }

    public static JSONObject getWords() throws IOException, JSONException {
        if (WORDS == null) {
            init(FunnyApplication.getFunnyContext());
        }
        return WORDS;
    }
}
