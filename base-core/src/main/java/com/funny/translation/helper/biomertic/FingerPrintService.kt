package com.funny.translation.helper.biomertic

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

interface FingerPrintService {
    @FormUrlEncoded
    @POST("user/get_finger_print_info")
    @Headers("Cache-Control: no-cache")
    suspend fun getFingerPrintInfo(
        @Field("username") username: String,
        @Field("did") did: String
    ): FingerPrintInfo

    @FormUrlEncoded
    @POST("user/save_finger_print_info")
    @Headers("Cache-Control: no-cache")
    suspend fun saveFingerPrintInfo(
        @Field("username") username: String,
        @Field("did") did: String,
        @Field("encrypted_info") encryptedInfo: String,
        @Field("iv") iv: String,
    )
}