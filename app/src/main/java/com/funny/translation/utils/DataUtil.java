package com.funny.translation.utils;

public class DataUtil {
    public static int[] coverStringToIntArray(String str){
        String[] arr=str.split("#");
        int[] result=new int[arr.length];
        for (int i = 0; i <arr.length ; i++) {
            result[i] = Integer.parseInt(arr[i]);
        }
        return result;
    }

    public static String coverIntArrayToString(int[] arr){
        StringBuilder sb = new StringBuilder();
        for (int item:arr) {
            sb.append(item);
            sb.append("#");
        }
        return sb.toString();
    }

    public static void setDefaultMapping(int[] arr){
        for (int i = 0; i < arr.length; i++) {
            arr[i] = i;
        }
    }

    public static int findStringIndex(String[] arr,String target){//找到一个东西在数组中的位置
        for (int i = 0; i < arr.length; i++) {
            if (target.equals(arr[i])){
                return i;
            }
        }
        return -1;
    }
}
