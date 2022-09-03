package com.funny.translation.translation;

import com.funny.translation.translate.TranslationException;

import java.util.HashMap;
import java.util.Map;

public class BaiduTransApi {
    private static final String TRANS_API_HOST = "http://api.fanyi.baidu.com/api/trans/vip/translate";
	private static BaiduTransApi api;
    private String appid;
    private String securityKey;

    public BaiduTransApi(String appid, String securityKey) {
        this.appid = appid;
        this.securityKey = securityKey;
    }

    public String getTransResult(String query, String from, String to) throws TranslationException {
        Map<String, String> params = buildParams(query, from, to);
        return BaiduHttpGet.get(TRANS_API_HOST, params);
    }

    private Map<String, String> buildParams(String query, String from, String to) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("q", query);
        params.put("from", from);
        params.put("to", to);

        params.put("appid", appid);

        // 随机数
        String salt = String.valueOf(System.currentTimeMillis());
        params.put("salt", salt);

        // 签名
        String src = appid + query + salt + securityKey; // 加密前的原文
        params.put("sign", MD5.md5(src));

        return params;
    }
	
	public static BaiduTransApi getBaiduTransApi(String appid, String securityKey){
		if(api==null){
			api=new BaiduTransApi(appid,securityKey);
		}
		return api;
	}

}
