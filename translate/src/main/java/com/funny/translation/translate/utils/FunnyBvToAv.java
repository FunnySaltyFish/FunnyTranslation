package com.funny.translation.translate.utils;

public class FunnyBvToAv
{
    //改编自 https://github.com/MOHJ0558/Bilibili_Conversion_between_av_and_BV/blob/master/avBV.cpp
    //该算法的最初版本使用极为罕见的WTFPL协议，感谢开源！
    //java 版 by FunnySaltyFish 2020.4.4
    static long xorn = 177451812L;
    static long add = 8728348608L;
    static int s[] = {11, 10, 3, 8, 4, 6};
    static String TABLE = "fZodR9XQDSUm21yCkr6zBqiveYah8bt4xsWpHnJE7jL5VG3guMTKNPAwcF";
    static long[] tr = new long[123];
    static{
        for (int i = 0; i < 58; i++) {
            tr[TABLE.charAt(i)] = i;
        }
    }

    public static String dec(String x) { //传 bv...
        long r = 0;
        for (int i = 0; i < 6; i++) {
            r += ((long)Math.pow(58, i) * tr[x.charAt(s[i])]);
        }
        long av=(r - add) ^ xorn;
        return "av"+av;
    }

    public static String enc(long x) { //仅传 av号
        x = (x ^ xorn) + add;
        char[] r = "BV1  4 1 7  ".toCharArray();
        for (int i = 0; i < 6; i++) {
            r[s[i]] = TABLE.charAt((int)Math.floor(x / (long)Math.pow(58,i) % 58));
        }
        return String.valueOf(r);
    }
}

