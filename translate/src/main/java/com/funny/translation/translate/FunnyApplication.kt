package com.funny.translation.translate

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.view.Gravity
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Offset
import com.funny.data_saver.core.DataSaverConverter.registerTypeConverters
import com.funny.translation.BaseApplication
import com.funny.translation.bean.UserInfoBean
import com.funny.translation.codeeditor.ui.editor.EditorSchemes
import com.funny.translation.helper.DeviceUtils
import com.funny.translation.helper.JsonX
import com.funny.translation.sign.SignUtils
import com.funny.translation.theme.ThemeType
import com.funny.translation.translate.ui.screen.TranslateScreen
import com.funny.translation.translate.ui.thanks.SponsorSortType
import com.funny.translation.translate.utils.FunnyUncaughtExceptionHandler
import com.funny.translation.translate.utils.SortResultUtils
import com.hjq.toast.ToastUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class FunnyApplication : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        ctx = this

        if (DeviceUtils.is64Bit()) {
            // 仅在 64 位时加载
            System.loadLibrary("monet")
        }

        FunnyUncaughtExceptionHandler.getInstance().init(ctx)
        ToastUtils.init(this)
        ToastUtils.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 260)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createScreenCaptureNotificationChannel()
        }

        CoroutineScope(Dispatchers.IO).launch {
            initLanguageDisplay(resources)
            SignUtils.loadJs()
            SortResultUtils.init()
        }

        // For ComposeDataSaver
        registerTypeConverters<UserInfoBean>(
            save = {
                JsonX.toJson(it)
            },
            restore = {
                // 修复旧版本 vip_start_time 为 Long 的问题
                try {
                    val matchResult = OLD_VIP_START_TIME.find(it)
                    if (matchResult != null)
                        JsonX.fromJson(it.replace(OLD_VIP_START_TIME, "$1:null"), UserInfoBean::class)
                    else
                        JsonX.fromJson(it, UserInfoBean::class)
                } catch (e: Exception){
                    e.printStackTrace()
                    UserInfoBean()
                }
            }
        )

        registerTypeConverters<EditorSchemes>(
            save = { it.name },
            restore = { EditorSchemes.valueOf(it) }
        )

        registerTypeConverters<SponsorSortType>(
            save = { it.name },
            restore = { SponsorSortType.valueOf(it) }
        )

        registerTypeConverters<Pair<String, Int>>(
            save = { "${it.first}:${it.second}" },
            restore = { val split = it.split(":"); Pair(split[0], split[1].toInt()) }
        )

        registerTypeConverters<Language>(
            save = { it.name },
            restore = { Language.valueOf(it) }
        )

        registerTypeConverters<Offset>(
            save = { "${it.x},${it.y}" },
            restore = { val split = it.split(",").map { it.toFloat() }; Offset(split[0], split[1])  }
        )

        registerTypeConverters<Uri?>(
            save = { it.toString() },
            restore = { if (it == "null") null else Uri.parse(it) }
        )

        registerTypeConverters<ThemeType>(save = ThemeType.Saver, restore = ThemeType.Restorer)
        registerTypeConverters<TranslateScreen>(save = TranslateScreen.Saver, restore = TranslateScreen.Restorer)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createScreenCaptureNotificationChannel() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        // Create the channel for the notification
        val screenCaptureChannel = NotificationChannel(SCREEN_CAPTURE_CHANNEL_ID, SCREEN_CAPTURE_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
        // Set the Notification Channel for the Notification Manager.
        notificationManager.createNotificationChannel(screenCaptureChannel)
    }

    companion object {
        var ctx by Delegates.notNull<FunnyApplication>()
        val resources: Resources get() = ctx.resources
        const val TAG = "FunnyApplication"

        internal const val SCREEN_CAPTURE_CHANNEL_ID = "CID_Screen_Capture"
        private const val SCREEN_CAPTURE_CHANNEL_NAME = "CNAME_Screen_Capture"
    }
}

val appCtx = FunnyApplication.ctx

private val OLD_VIP_START_TIME = """("vip_start_time"):(-?\d+)""".toRegex()