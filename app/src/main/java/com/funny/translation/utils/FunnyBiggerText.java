package com.funny.translation.utils;

import android.content.Context;

import com.funny.translation.bean.Consts;
import com.funny.translation.translation.TranslationException;

import java.io.InputStream;

public class FunnyBiggerText {
    static Context context;
    private final static String ENCODE = "GB2312";
    private final static String ZK16 = "HZK16";

    private static boolean[][] arr;
    static int all_16_32 = 16;
    static int all_2_4 = 2;
    static int all_32_128 = 32;

    public static String drawString(Context context,String str) throws TranslationException {
        FunnyBiggerText.context = context;
        byte[] data = null;
        int[] code = null;
        int byteCount;
        int lCount;

        int curCharNumber = 0;

        arr = new boolean[all_16_32][all_16_32*2];
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < str.length(); i++) {
            char curChar = str.charAt(i);
            if (curChar < 0x80) {
                continue;
            }
            code = getByteCode(str.substring(i, i + 1));
            data = read(code[0], code[1]);
            byteCount = 0;
            for (int line = 0; line < all_16_32; line++) {
                lCount = 0;
                curCharNumber = line/all_16_32;


                for (int k = 0; k < all_2_4; k++) {
                    for (int j = 0; j < 8; j++) {
                        if (((data[byteCount] >> (7 - j)) & 0x1) == 1) {
                            arr[line][lCount*2] = true;
                            //chars[line][lCount*2] = curChar;
                            //System.out.print("@");
                            sb.append(curChar);
                        } else {
                            //System.out.print('　');
                            arr[line][lCount*2] = false;
                            sb.append('　');
                            //chars[line][lCount*2] = ' ';
                        }
                        sb.append('　');
                        lCount++;
                    }
                    byteCount++;
                    //System.out.println("byteCount is :"+byteCount);
                }
                //System.out.println();
                sb.append("\n");

            }
        }
        return sb.toString();
    }

    protected static byte[] read(int areaCode, int posCode) throws TranslationException{
        byte[] data = null;
        try {
            int area = areaCode - 0xa0;
            int pos = posCode - 0xa0;
            InputStream in = context.getResources().getAssets().open(ZK16);
            long offset = all_32_128 * ((area - 1) * 94 + pos - 1);
            in.skip(offset);
            data = new byte[all_32_128];
            in.read(data, 0, all_32_128);
            in.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TranslationException(Consts.ERROR_UNKNOWN);
        }
        return data;
    }

    protected static int[] getByteCode(String str) throws TranslationException{
        int[] byteCode = new int[2];
        try {
            byte[] data = str.getBytes(ENCODE);
            byteCode[0] = data[0] < 0 ? 256 + data[0] : data[0];
            byteCode[1] = data[1] < 0 ? 256 + data[1] : data[1];
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TranslationException(Consts.ERROR_UNKNOWN);
        }
        return byteCode;
    }
}
