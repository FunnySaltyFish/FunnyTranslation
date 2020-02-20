package com.funny.translation.utils;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.funny.translation.bean.Consts;
public class StringUtil
{
	public static String md5(String pwd) throws Exception {
        // 创建MessageDigest对象
        MessageDigest digest = MessageDigest.getInstance("MD5");
        // 对明文进行加密
        byte[] temp = digest.digest(pwd.getBytes());
        // 准备StringBuilder用于保存结果
        StringBuilder builder = new StringBuilder();
        // 遍历字节数组, 一个字节转换为长度为2的字符串
        for (byte b : temp) {
            // 去除负数
            String s = Integer.toHexString(b & 0xff);
            // 补零
            if(s.length() == 1) {
                builder.append(0);
            }
            builder.append(s);
        }
        return builder.toString();
    }
	
	public static String unicodeToString(String str) {  
	    Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");  
	    Matcher matcher = pattern.matcher(str);  
	    char ch;  
	    while (matcher.find()) {  
	        String group = matcher.group(2);  
	        ch = (char) Integer.parseInt(group, 16);
	        String group1 = matcher.group(1);  
	        str = str.replace(group1, ch + "");  
	    }  
	    return str;  
	}  
	
	public static boolean isUnicode(String str){//是否是unicode
		Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");  
	    Matcher matcher = pattern.matcher(str);
		return matcher.matches();
	}
	
	public static short getLanguage(String str){
		int cnTimes=0,enTimes=0;//中文/英文出现次数
		int punctuationTimes=0;
		int otherTimes=0;
		Pattern cn=Pattern.compile("[\\u4E00-\\u9FA5]");
		Matcher cnMatcher=cn.matcher(str);
		while(cnMatcher.find()){
			cnTimes++;
		}
		//English
		Pattern en=Pattern.compile("[A-Za-z]+?\\b");
		Matcher enMatcher=en.matcher(str);
		while(enMatcher.find()){
			enTimes++;
		}
		//标点符号 空白符号
		Pattern punctuation=Pattern.compile("[\\s\\p{Zs}`~!@#$%^&*()-_+={[}]|\\:;\"',.>/?·！@#￥%……&*（）——+【】；：’‘“”，《。》、？]");
		Matcher pMatcher=punctuation.matcher(str);
		while(pMatcher.find()){
			punctuationTimes++;
		}
		
		//enTimes/=2;
		//String noCnEnStr=cnMatcher.replaceAll("");
		//noCnEnStr=enMatcher.replaceAll("");
		//System.out.println(noCnEnStr);
		otherTimes=str.length()-enTimes-cnTimes-punctuationTimes;
		System.out.printf("cn:%d en:%d other:%d\n",cnTimes,enTimes,otherTimes);
		if(otherTimes>enTimes&&otherTimes>cnTimes){
			return -1;
		}
		else if(enTimes>cnTimes){
			return Consts.LANGUAGE_ENGLISH;
		}else{
			return Consts.LANGUAGE_CHINESE;
		}
		
	}
	
	
}
