package com.funny.translation.js;

import android.util.Log;

import com.funny.translation.bean.Consts;
import com.funny.translation.translation.TranslationResult;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

public class JSEngine {
    private Class clazz;
    private Context jsContext;
    private Scriptable scope;
    public NativeObject funnyJS;

    public JS js;

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

    public JSEngine(){
        this.clazz = JSEngine.class;
    }

    public void loadJS(JS js){
        this.js = js;
        this.fileName = js.fileName;
        String baseJSCode = "var ScriptAPI = java.lang.Class.forName(\"" + clazz.getName() + "\", true, javaLoader);";
        this.code = baseJSCode +js.code;
    }

    public void request(TranslationCustom translationCustom) throws JSException{
        jsContext = Context.enter();
        jsContext.setOptimizationLevel(-1);
        try{
            scope = jsContext.initStandardObjects();
            funnyJS.setParentScope(scope);
            //设置当前类为上下文已经获取当前类的加载器
            ScriptableObject.putProperty(scope,"javaContext",Context.javaToJS(this,scope));
            ScriptableObject.putProperty(scope,"javaLoader",Context.javaToJS(clazz.getClassLoader(),scope));
            //ScriptableObject.putProperty(scope,"FunnyAPI",Context.javaToJS(new FunnyAPI(),scope));
            ScriptableObject.defineClass(scope,FunnyAPI.class);
            ScriptableObject.defineClass(scope,JSTranslationResult.class);
            //ScriptableObject.defineClass(scope,JSConsts.class);

            scope.put("sourceString",scope,Context.javaToJS(translationCustom.sourceString,scope));
            scope.put("sourceLanguage",scope,Context.javaToJS(translationCustom.sourceLanguage,scope));
            scope.put("targetLanguage",scope,Context.javaToJS(translationCustom.targetLanguage,scope));
            //scope.put("sourceString",scope,Context.javaToJS(translationCustom.,scope));

            Object x = jsContext.evaluateString(scope, code ,fileName,1,null);
            Object funnyJS1 = scope.get("FunnyJS",scope);
            if(funnyJS1 instanceof NativeObject){
                //2021 3 2 这个地方因为funnyJS为之前的一直错误（返回的值是之前的)，纠结了两天才找到bug……
                funnyJS = (NativeObject)funnyJS1;
            }
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
            throw new JSException(Consts.ERROR_UNKNOWN);
        } catch (RhinoException e){
            e.printStackTrace();
            String error = String.format(Locale.CHINA,"JS运行错误！详细的原因是：\n第%d行：%s",e.lineNumber(),e.details());
            throw new JSException(error);
        }
    }

    public void loadBasicConfigurations() throws JSException{
        Context tempContext = Context.enter();
        tempContext.setOptimizationLevel(-1);
        try{
            Scriptable tempScope = tempContext.initStandardObjects();
            //设置当前类为上下文已经获取当前类的加载器
            ScriptableObject.putProperty(tempScope,"javaContext",Context.javaToJS(this,tempScope));
            ScriptableObject.putProperty(tempScope,"javaLoader",Context.javaToJS(clazz.getClassLoader(),tempScope));
            //ScriptableObject.putProperty(scope,"FunnyAPI",Context.javaToJS(new FunnyAPI(),scope));
            ScriptableObject.defineClass(tempScope,FunnyAPI.class);
            ScriptableObject.defineClass(tempScope,JSTranslationResult.class);
            ScriptableObject.defineClass(tempScope,JSConsts.class);

            tempScope.put("sourceString",tempScope,Context.javaToJS("默认的",tempScope));
            tempScope.put("sourceLanguage",tempScope,Context.javaToJS("源语言",tempScope));
            tempScope.put("targetLanguage",tempScope,Context.javaToJS("目标语言",tempScope));

            Object x = tempContext.evaluateString(tempScope, code ,fileName,1,null);
            Object funnyJS1 = tempScope.get("FunnyJS",tempScope);
            if(funnyJS1 instanceof NativeObject){

                funnyJS = (NativeObject)funnyJS1;
                js.version = ((Double)funnyJS.get("version")).intValue();
                js.about = (String) funnyJS.get("about");
                js.author = (String) funnyJS.get("author");
            }else{
                throw new JSException(String.format("解析配置文件时出错！请确保插件配置文件正确！\n@JS[version:%s,author:%s]",js.version,js.author));
            }
            //Log.i("JSEngine",funnyJS.toString());

        } catch (RhinoException e){
            e.printStackTrace();
            String error = String.format(Locale.CHINA,"JS运行错误！详细的原因是：\n第%d行：%s",e.lineNumber(),e.details());
            throw new JSException(error);
        }catch (Exception e){
            e.printStackTrace();
            throw new JSException("JS预加载时发生未知错误！");
        }finally {
            Context.exit();
        }
    }

    public Object callFunnyJSFunction(String functionName,Object[] params) throws JSException{
        try {
            Object object = funnyJS.get(functionName);
            if(object instanceof Function){
                Function function = (Function)object;
                return function.call(jsContext,scope,scope,params);
            }else throw new JSException("方法["+functionName+"]未定义！");
        }catch (RhinoException e){
            String error = String.format(Locale.CHINA,"JS运行错误！详细的原因是：\n第%d行：%s",e.lineNumber(),e.details());
            throw new JSException(error);
        }
    }

    public Object callFunction(String functionName,Object[] params) throws JSException{
        try {
            Object object = scope.get(functionName,scope);
            if(object instanceof Function){
                Function function = (Function)object;
                return function.call(jsContext,scope,scope,params);
            }else throw new JSException("方法["+functionName+"]未定义！");
        }catch (RhinoException e){
            String error = String.format(Locale.CHINA,"JS运行错误！详细的原因是：\n第%d行：%s",e.lineNumber(),e.details());
            throw new JSException(error);
        }

    }

    public String callFunctionReturnString(String functionName,Object[] params) throws JSException{
        Object r = callFunction(functionName,params);
        if(r instanceof Undefined){
            return "";
        }
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
