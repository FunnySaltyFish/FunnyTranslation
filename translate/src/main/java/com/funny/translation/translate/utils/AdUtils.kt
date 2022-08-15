package com.funny.translation.translate.utils

import com.funny.translation.translate.appCtx
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions

object AdUtils {
    fun init(){
        val adLoader = AdLoader.Builder(appCtx, "ca-app-pub-3940256099942544/2247696110")
            .forNativeAd { ad ->
                
            }
            .withAdListener(object : AdListener() {

            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                // Methods in the NativeAdOptions.Builder class can be
                // used here to specify individual options settings.
                .build())
            .build()

    }
}