package com.funny.translation.translate.network

import com.funny.translation.network.ServiceCreator
import com.funny.translation.translate.network.service.*

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

    val noticeService by lazy {
        ServiceCreator.create(NoticeService::class.java)
    }

    val imageTranslateService by lazy {
        ServiceCreator.create(ImageTranslateService::class.java)
    }

    val vipService by lazy {
        ServiceCreator.create(VipService::class.java)
    }
}