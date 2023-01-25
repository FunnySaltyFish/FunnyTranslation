package com.funny.translation.translate.network.service

import com.funny.translation.network.CommonData
import com.funny.translation.translate.ImageTranslationResult
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ImageTranslateService {
    @POST("api/translate_image")
    suspend fun getTransResult(
        @Body body: RequestBody
    ): CommonData<ImageTranslationResult>
}