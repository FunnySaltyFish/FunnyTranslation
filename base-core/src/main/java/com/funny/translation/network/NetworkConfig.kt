package com.funny.translation.network

object NetworkConfig {

    const val API_HOST = "api.funnysaltyfish.fun"
    const val TRANS_PATH = "/trans/v1/"
    val BASE_URL = OkHttpUtils.removeExtraSlashOfUrl("https://$API_HOST/$TRANS_PATH")
}