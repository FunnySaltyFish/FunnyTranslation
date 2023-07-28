package com.funny.translation.translate.network.service

import com.funny.translation.translate.bean.RecommendApp
import retrofit2.http.GET

interface AppRecommendationService {
    @GET("api/get_recommend_app")
    suspend fun getRecommendApp() : List<RecommendApp>
}