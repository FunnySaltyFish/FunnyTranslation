package com.funny.translation.translate;

import java.util.Base64;

public class Base64Test {
    public static String MAPPING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    static  {
        MAPPING += MAPPING.toLowerCase();
        MAPPING += "0123456789";
        MAPPING += "+/";
        System.out.println(MAPPING.length());
    }

    public static String encodeToBase64(String text) {
        byte[] bytes = text.getBytes();
        int l = bytes.length;

        int paddingLength = (3 - l % 3) % 3;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < l; i += 3){
            int triple = (bytes[i] & 0xff) << 16;
            if (i + 1 < l) triple |= (bytes[i+1] & 0xff) << 8;
            if (i + 2 < l) triple |= (bytes[i+2] & 0xff);
            sb.append(MAPPING.charAt((triple >> 18) & 0x3f));
            sb.append(MAPPING.charAt((triple >> 12) & 0x3f));
            sb.append(MAPPING.charAt((triple >> 6) & 0x3f));
            sb.append(MAPPING.charAt((triple) & 0x3f));
        }

        for (int i = 0; i < paddingLength; i++) {
            sb.setCharAt(sb.length()-1-i, '=');
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        String s = "Hello，世界";
        String t = encodeToBase64(s);
        System.out.println(t);
        var res = new String(Base64.getEncoder().encode(s.getBytes()));
        System.out.println(res);
        System.out.println(res.equals(t));
    }

}
