package com.funny.translation.utils;

public class AutoCompleteUtil {
    public static String getCurrentText(String fullText,int currentCursor) {
        String result = "";
        int i = currentCursor;
        int l=fullText.length();
        int end;
        while(i>0&&(fullText.charAt(i)<32||fullText.charAt(i)>47)){
            i--;
        }
        if(i==0){
            return fullText.substring(0,currentCursor+1);
        }else{
            end = currentCursor+1;
            if(end<0)end = l;
            if(i<=end)result = fullText.substring(i==0?i:i+1, end);
        }
        return result;
    }
}
