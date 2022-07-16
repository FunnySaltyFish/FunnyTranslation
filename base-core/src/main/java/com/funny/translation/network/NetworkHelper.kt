package com.funny.translation.network

object NetworkHelper {
    fun isConnected(): Boolean = try {
        NetworkReceiver.getNetworkType() >= 0
    } catch (e : Exception){
        e.printStackTrace()
        false
    }
}