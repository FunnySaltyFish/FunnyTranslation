package com.funny.translation.utils;
import android.support.annotation.NonNull;

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
	
	//RC4
	public static String HloveyRC4(String aInput,String aKey)   
    {   
        int[] iS = new int[256];   
        byte[] iK = new byte[256];   

        for (int i=0;i<256;i++)   
            iS[i]=i;   

        int j = 1;   

        for (short i= 0;i<256;i++)   
        {   
            iK[i]=(byte)aKey.charAt((i % aKey.length()));   
        }   

        j=0;   

        for (int i=0;i<255;i++)   
        {   
            j=(j+iS[i]+iK[i]) % 256;   
            int temp = iS[i];   
            iS[i]=iS[j];   
            iS[j]=temp;   
        }   


        int i=0;   
        j=0;   
        char[] iInputChar = aInput.toCharArray();   
        char[] iOutputChar = new char[iInputChar.length];   
        for(short x = 0;x<iInputChar.length;x++)   
        {   
            i = (i+1) % 256;   
            j = (j+iS[i]) % 256;   
            int temp = iS[i];   
            iS[i]=iS[j];   
            iS[j]=temp;   
            int t = (iS[i]+(iS[j] % 256)) % 256;   
            int iY = iS[t];   
            char iCY = (char)iY;   
            iOutputChar[x] =(char)( iInputChar[x] ^ iCY) ;      
        }   

        return new String(iOutputChar);   

    }

    public static String replaceEnglishPunctuation(String text){
		String result = text;
		result = result.replaceAll(",","，");
		result = result.replaceAll("\\.","：");
		result = result.replaceAll(";","；");
		result = result.replaceAll(" ","");
		return result;
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

	public static boolean isValidContent(String text){
		if(text == null || text.isEmpty()){
			return false;
		}
		if (text.replaceAll("[\\s]","").equals("")){
			return false;
		}
		return true;
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
		Pattern punctuation=Pattern.compile("[\\s\\p{Zs}`~!@#$%^&*()-_+={[}]|:;\"',.>/?·！￥…（）—【】；：’‘“”，《。》、？]");
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

	@NonNull
	public static String extraChinese(String sourceText){
		StringBuilder sb =new StringBuilder();
		Pattern cn=Pattern.compile("[\\u4E00-\\u9FA5]+");
		Matcher enMatcher=cn.matcher(sourceText);
		while(enMatcher.find()){
			sb.append(enMatcher.group(0));
		}
		return sb.toString();
	}

	public static String insertJuhao(@NonNull String text){
		char[] sourceChars = text.toCharArray();
		char[] result = new char[text.length()*2];
		for (int i = 0; i < text.length(); i++) {
			result[i*2] = sourceChars[i];
			result[i*2+1] = '。';
		}
		return String.valueOf(result);
	}

	public static boolean isNumber(String str){
		Pattern p=Pattern.compile("^\\d+$");
		Matcher m=p.matcher(str);
		if(m.find()){
			return true;
		}
		return false;
	}

	public static long findAv(String str){//返回数字
		Pattern p=Pattern.compile("av(\\d+)",Pattern.CASE_INSENSITIVE);
		Matcher m=p.matcher(str);
		if(m.find()){
			return Long.valueOf(m.group(1));
		}
		return -1;
	}

	public static String findBv(String str){//返回 bv...
		Pattern p=Pattern.compile("bv\\w+",Pattern.CASE_INSENSITIVE);
		Matcher m=p.matcher(str);
		if(m.find()){
			return (m.group(0));
		}
		return "";
	}
	
}
