package com.funny.translation.translate.network.service
import com.funny.translation.translate.ui.thanks.Sponsor
import retrofit2.http.GET

interface SponsorService {
    @GET("sponsor/get_all")
    suspend fun getAllSponsor() : List<Sponsor>
}