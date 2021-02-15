package com.funny.translation.js;

import java.util.ArrayList;

public class JSManager {
    public static ArrayList<Integer> currentRunningJSList;
    public static short currentRunningJSId = 99;

    public static void init(){
        currentRunningJSList = new ArrayList<>();
    }

    public static short getJSEngineKind(){
        return currentRunningJSId;
    }
}
