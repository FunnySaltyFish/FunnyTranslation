package com.funny.translation.translate.network.service

import com.funny.translation.translate.bean.UpdateInfo
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST



interface AppUpdateService {
    @FormUrlEncoded
    @POST("app_update/check_update")
    suspend fun getUpdateInfo(
        @Field("version_code") versionCode : Long,
        @Field("channel") channel : String
    ) : UpdateInfo
}