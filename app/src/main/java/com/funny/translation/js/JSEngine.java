package com.funny.translation.js;

import android.util.Log;

import com.funny.translation.translation.TranslationResult;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.lang.reflect.InvocationTargetException;

public class JSEngine {
    private Class clazz;
    private Context jsContext;
    private Scriptable scope;

    private String code;
    private String fileName;
//    private String testCode = "var methodGetValue = ScriptAPI.getMethod(\"getValue\",[java.lang.Object]);\n" +
//            "function get(str){return methodGetValue.invoke(javaContext,str);}" +
//            "var a = get(\"Hello World\");" +
//            "var methodPrint = ScriptAPI.getMethod(\"print\",[java.lang.Object]);" +
//            "function print(a){methodPrint.invoke(javaContext,a);}" +
//            "FunnyAPI.printSelf();" +
//            "print(FunnyAPI);" +
//            "var ctx = FunnyAPI.getActivity();" +
//            "print(ctx);";

    public JSEngine(String fileName,String otherCode){
        this.clazz = JSEngine.class;
        this.fileName = fileName;
        String baseJSCode = "var ScriptAPI = java.lang.Class.forName(\"" + fileName + "\", true, javaLoader);\n";
        code = baseJSCode +otherCode;
    }

    public void request(){
        jsContext = Context.enter();
        jsContext.setOptimizationLevel(-1);
        try{
            scope = jsContext.initStandardObjects();
            //设置当前类为上下文已经获取当前类的加载器
            ScriptableObject.putProperty(scope,"javaContext",Context.javaToJS(this,scope));
            ScriptableObject.putProperty(scope,"javaLoader",Context.javaToJS(clazz.getClassLoader(),scope));
            //ScriptableObject.putProperty(scope,"FunnyAPI",Context.javaToJS(new FunnyAPI(),scope));
            ScriptableObject.defineClass(scope,FunnyAPI.class);
            ScriptableObject.defineClass(scope, JSTranslationResult.class);

            Object x = jsContext.evaluateString(scope, code ,clazz.getSimpleName(),1,null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } finally {
            Context.exit();
        }

    }

    public Object callFunction(String functionName,Object[] params) throws JSException{
        Object object = scope.get(functionName,scope);
        if(object instanceof Function){
            Function function = (Function)object;
            return function.call(jsContext,scope,scope,params);
        }else throw new JSException("方法["+functionName+"]未定义！");
    }

    public String callFunctionReturnString(String functionName,Object[] params) throws JSException{
        Object r = callFunction(functionName,params);
        return (String)r;
    }

    public String getValue(Object c){
        print("getValue:C is "+c);
        return "从getValue获得的值";
    }

    //

    public void print(Object c){
        Log.i("JSEngine","JSMessage:"+c);
    }

}
