package com.funny.translation.translate.network.service

import com.funny.translation.js.bean.JsBean
import retrofit2.http.GET

interface PluginService {
    @GET("plugin/get_all")
    suspend fun getOnlinePlugins() : List<JsBean>
}