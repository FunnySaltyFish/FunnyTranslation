package com.funny.translation.js;

import com.funny.translation.bean.Consts;
import com.funny.translation.bean.LanguageBean;
import com.funny.translation.js.bean.JsBean;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSUtils {
    public static ArrayList<LanguageBean> coverJSToLanguageBean(ArrayList<JsBean> jsBeanList){
        ArrayList<LanguageBean> result=new ArrayList<>();
        int i=0;
        for (JsBean jsBean : jsBeanList){
            JSLanguageBean bean = new JSLanguageBean();
            bean.text = jsBean.getFileName();
            bean.setUserData((short)i);
            bean.setCheckKind(Consts.CHECK_MULTI);
            bean.setId(jsBean.getId());
            result.add(bean);
            i++;
        }
        return result;
    }

    public static JSONObject extractConfiguration(String code) throws JSException {
        Pattern patternAll = Pattern.compile(".{2}FunnyTranslation JS Engine Start.{2}(.+).{2}FunnyTranslation JS Engine End.{2}",Pattern.DOTALL);
        Matcher matcherAll = patternAll.matcher(code);
        JSONObject result = null;
        boolean hasFind = false;
        if(matcherAll.find()){
            String content = matcherAll.group(1);
            Pattern patternCode = Pattern.compile("\\{.+\\}",Pattern.DOTALL);
            Matcher matcherCode = patternCode.matcher(content);
            if(matcherCode.find()){
                String json = matcherCode.group(0);
                hasFind = true;
                try {
                    result = new JSONObject(json);
                } catch (JSONException e) {
                    e.printStackTrace();
                    throw new JSException("配置文件内容出错！请确保插件包含正确的FunnyJS变量！");
                }
            }
        }
        if (!hasFind)throw new JSException("未找到正确的配置文件！请确保插件包含正确的FunnyJS变量！");
        return result;
    }
}
