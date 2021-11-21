package com.funny.translation.translate.network

import com.funny.translation.network.ServiceCreator
import com.funny.translation.translate.network.service.AppUpdateService
import com.funny.translation.translate.network.service.SponsorService



object TransNetwork {
    val sponsorService = ServiceCreator.create(SponsorService::class.java)
    val appUpdateService = ServiceCreator.create(AppUpdateService::class.java)
}