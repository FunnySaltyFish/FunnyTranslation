package com.funny.translation.utils;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import android.telephony.TelephonyManager;

/**
 * 网络工具类
 * Created by zhoujingxin on 2018/1/23.
 
 来自掘金 https://juejin.im/post/5a6d6292518825733c144bb5
 */

public class NetworkUtil{

    public static final int NETWORK_TYPE_NONE = 0;
    public static final int NETWORK_TYPE_WIFI = 1;
    public static final int NETWORK_TYPE_2G = 2;
    public static final int NETWORK_TYPE_3G = 3;
    public static final int NETWORK_TYPE_4G = 4;
    public static final int NETWORK_TYPE_5G = 5;

    /**
     * 判断当前是否有网络连接
     *
     * @param context
     * @return true：数据网络或者wifi；false：没有开启任何网络
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo != null) {
                return networkInfo.isAvailable() && networkInfo.isConnected();
            }
        }

        return false;
    }

    /**
     * Wifi网络当前是否连接
     *
     * @param context
     * @return 只开启wifi时，返回true；同时开启wifi和数据网络时，返回true；
     */
    public static boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return networkInfo.isAvailable() && networkInfo.isConnected();
            }
        }

        return false;
    }

    /**
     * 数据网络当前是否连接
     *
     * @param context
     * @return 只开启数据网络时，返回true；同时开启wifi和数据网络时会返回false；
     */
    public static boolean isMobileConnected(Context context) {
        if (context != null) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                return networkInfo.isAvailable() && networkInfo.isConnected();
            }
        }

        return false;
    }

    /**
     * 通过反射获取数据网络是否连接
     * 可以在wifi和数据网络同时开启时使用
     * @param context
     * @return
     */
    public static boolean isMobileDataEnabled(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Method getMobileDataEnabled = ConnectivityManager.class.getDeclaredMethod("getMobileDataEnabled");
            getMobileDataEnabled.setAccessible(true);
            boolean dataEnabled = (boolean) getMobileDataEnabled.invoke(connectivityManager);
            return dataEnabled;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 获取当前连接的网络的类型
     * 原生
     *
     * @param context
     * @return
     */
    public static int getConcreteNetworkType(Context context) {
        if (context != null) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable()) {
                return networkInfo.getType();
            }
        }

        return -1;
    }

    /**
     * 获取当前的网络状态 ：没有网络-0：WIFI网络1：4G网络-4：3G网络-3：2G网络-2
     * 自定义
     *
     * @param context
     * @return
     */
    public static int getSimpleNetworkType(Context context) {
        //结果返回值
        int netType = NETWORK_TYPE_NONE;
        //获取手机所有连接管理对象
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //获取NetworkInfo对象
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        //NetworkInfo对象为空 则代表没有网络
        if (networkInfo == null) {
            return netType;
        }
        //否则 NetworkInfo对象不为空 则获取该networkInfo的类型
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_WIFI) {
            //WIFI
            netType = NETWORK_TYPE_WIFI;
        } else if (nType == ConnectivityManager.TYPE_MOBILE) {
            int nSubType = networkInfo.getSubtype();
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            //3G   联通的3G为UMTS或HSDPA 电信的3G为EVDO
            if (nSubType == TelephonyManager.NETWORK_TYPE_NR&&!telephonyManager.isNetworkRoaming()){
                netType = NETWORK_TYPE_5G;
            }
            else if (nSubType == TelephonyManager.NETWORK_TYPE_LTE
				&& !telephonyManager.isNetworkRoaming()) {
                netType = NETWORK_TYPE_4G;
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_UMTS
					   || nSubType == TelephonyManager.NETWORK_TYPE_HSDPA
					   || nSubType == TelephonyManager.NETWORK_TYPE_EVDO_0
					   && !telephonyManager.isNetworkRoaming()) {
                netType = NETWORK_TYPE_3G;
                //2G 移动和联通的2G为GPRS或EGDE，电信的2G为CDMA
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_GPRS
					   || nSubType == TelephonyManager.NETWORK_TYPE_EDGE
					   || nSubType == TelephonyManager.NETWORK_TYPE_CDMA
					   && !telephonyManager.isNetworkRoaming()) {
                netType = NETWORK_TYPE_2G;
            } else {
                netType = NETWORK_TYPE_2G;
            }
        }
        return netType;
    }

	}

