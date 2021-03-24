package com.funny.translation.js;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class JSManager {
    public static ArrayList<JSEngine> currentRunningJSList = new ArrayList<>();
    public static JSEngine currentRunningJSEngine;
    public static short currentRunningJSId = 99;

    public static short getJSEngineKind(){
        return currentRunningJSId;
    }

    public static void addJSEngine(JSEngine engine){
        if(!currentRunningJSList.contains(engine)){
            currentRunningJSList.add(engine);
        }
    }

    public static void clearAllEngines(){
        currentRunningJSList.clear();
    }

    @Nullable
    public static JSEngine getJSEngineById(int id){
        for (JSEngine jsEngine:currentRunningJSList){
            if (jsEngine.js.id==id)return jsEngine;
        }
        return null;
    }
}
