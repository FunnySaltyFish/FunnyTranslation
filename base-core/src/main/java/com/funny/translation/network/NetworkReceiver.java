package com.funny.translation.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.Objects;

/**
 * @author MrLiu
 * @date 2020/5/15
 * desc 广播接收者
 */
public class NetworkReceiver extends BroadcastReceiver {
    private static long WIFI_TIME = 0;
    private static long ETHERNET_TIME = 0;
    private static long NONE_TIME = 0;
    private static int LAST_TYPE = -3;
    private static final String TAG = "TAG";

    @Override
    public void onReceive(Context context, Intent intent) {
        // 监听网络连接，包括wifi和移动数据的打开和关闭,以及连接上可用的连接都会接到监听
        // 特殊注意：如果if条件生效，那么证明当前是有连接wifi或移动网络的，如果有业务逻辑最好把else场景酌情考虑进去！
        if (Objects.equals(intent.getAction(), ConnectivityManager.CONNECTIVITY_ACTION)) {
            setNetworkState(context);
        }
    }

    public static void setNetworkState(Context context) {
        long time = System.currentTimeMillis();
        if (time != WIFI_TIME && time != ETHERNET_TIME && time != NONE_TIME) {
            final int netWorkState = getNetWorkState(context);
            Log.d(TAG, "onReceive: networkState : " + netWorkState);
            if (netWorkState == 0 && LAST_TYPE != 0) {
                WIFI_TIME = time;
                LAST_TYPE = netWorkState;
                Log.e(TAG, "wifi：" + time);
            } else if (netWorkState == 1 && LAST_TYPE != 1) {
                ETHERNET_TIME = time;
                LAST_TYPE = netWorkState;
                Log.e(TAG, "数据网络：" + time);
            } else if (netWorkState == -1 && LAST_TYPE != -1) {
                NONE_TIME = time;
                LAST_TYPE = netWorkState;
                Log.e(TAG, "无网络：" + time);
            }
        }
    }
    
    private static final int NETWORK_NONE = -1; //无网络连接
    private static final int NETWORK_WIFI = 0; //wifi
    private static final int NETWORK_MOBILE = 1; //数据网络

    //判断网络状态与类型
    public static int getNetWorkState(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_WIFI)) {
                return NETWORK_WIFI;
            } else if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_MOBILE)) {
                return NETWORK_MOBILE;
            }
        } else {
            return NETWORK_NONE;
        }
        return NETWORK_NONE;
    }

    public static int getNetworkType(){
        return LAST_TYPE;
    }
}

// 作者：Shanghai_Liu
// 链接：https://juejin.cn/post/7011004836590288933
// 来源：稀土掘金
// 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。