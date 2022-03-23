package com.funny.translation.network

import android.util.Log
import com.funny.translation.BaseApplication
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException


class HttpCacheInterceptor : Interceptor {
    companion object{
        const val TAG = "HttpCacheInterceptor"
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request: Request = chain.request()
        val connected = NetworkHelper.isConnected(BaseApplication.ctx)
        Log.d(TAG, "intercept: connected:$connected")
        if (!connected) {
            request = request.newBuilder()
                .cacheControl(CacheControl.FORCE_CACHE)
                .build()
        }
        val response: Response = chain.proceed(request)
        if (connected) {
            val maxAge = 5*60 // read from cache for 5 minute
            response.newBuilder()
                .removeHeader("Pragma")
                .header("Cache-Control", "public, max-age=$maxAge")
                .build()
        } else {
            val maxStale = 60 * 60 * 24 * 28 // tolerate 4-weeks stale
            response.newBuilder()
                .removeHeader("Pragma")
                .header("Cache-Control", "public, only-if-cached, max-stale=$maxStale")
                .build()
        }
        return response
    }
}