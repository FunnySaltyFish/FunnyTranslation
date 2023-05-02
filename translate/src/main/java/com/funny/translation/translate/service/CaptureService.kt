package com.funny.translation.translate.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.funny.translation.translate.FunnyApplication

class CaptureService : Service() {
    override fun onCreate() {
        super.onCreate()
        startForeground(1, NotificationCompat.Builder(this, FunnyApplication.SCREEN_CAPTURE_CHANNEL_ID).build())
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}