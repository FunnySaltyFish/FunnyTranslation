package com.funny.translation.bean;

import android.util.Log;

import java.util.HashMap;

public class DoubleMap {
    HashMap one,two;
    String TAG = "DoubleMap";
    public DoubleMap(){
        one = new HashMap<Integer,Integer>();
        two = new HashMap<Integer,Integer>();
    }

    public DoubleMap(Object[] arr){
        one = new HashMap<Integer,Integer>();
        two = new HashMap<Integer,Integer>();
        set(arr);
    }

    public DoubleMap(int[] arr){
        one = new HashMap<Integer,Integer>();
        two = new HashMap<Integer,Integer>();
        set(arr);
    }

    public void set(Object[] arr){
        for (int i = 0; i < arr.length; i++) {
            one.put(i,arr[i]);//正向
            two.put(arr[i],i);
        }
    }

    public void set(int[] arr){
        for (int i = 0; i < arr.length; i++) {
            one.put(i,arr[i]);//正向
            two.put(arr,i);
        }
    }

    public int getByKey(int key){
        return (Integer)one.get(key);
    }

    public int getByValue(int value){
        Log.i(TAG,"-getByValue() value is "+value);
        return (Integer)two.get(value);
    }
}
