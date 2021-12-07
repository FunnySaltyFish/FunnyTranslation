package com.funny.translation.translate.network

import com.funny.translation.network.ServiceCreator
import com.funny.translation.translate.network.service.AppUpdateService
import com.funny.translation.translate.network.service.PluginService
import com.funny.translation.translate.network.service.SponsorService

object TransNetwork {
    val sponsorService by lazy {
        ServiceCreator.create(SponsorService::class.java)
    }

    val appUpdateService by lazy {
        ServiceCreator.create(AppUpdateService::class.java)
    }

    val pluginService by lazy {
        ServiceCreator.create(PluginService::class.java)
    }
}