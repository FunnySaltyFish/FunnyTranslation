package com.funny.translation.js;

import android.util.Log;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class FunnyJSEngine {
    Context jsContext;
    Scriptable script;
    public FunnyJSEngine(){

    }

    public void request(){
        jsContext = Context.enter();
        try {
            jsContext.setOptimizationLevel(-1);
            script = jsContext.initSafeStandardObjects();
            String code = "var a = 99;\nfunction test(str){return \"这里是test函数，参数为：\"+str;}\n";
            jsContext.evaluateString(script, code, "test1.js", 1, null);

            Object a = script.get("a",script);
            if(a == Scriptable.NOT_FOUND){
                print("未发现 a 变量");
            }else {
                print("a的值是"+a);
            }

            Object test = script.get("test",script);
            if(!(test instanceof Function)){
                print("test不是个函数");
            }else{
                Function testFunction = (Function)test;
                print(testFunction.call(jsContext,script,script,new Object[]{"Hello World"}));
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            Context.exit();
        }
    }

    private void print(Object str){
        Log.i("FunnyJSEngine",str.toString());
    }

}
