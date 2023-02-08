package com.funny.translation.translate

import android.content.res.Resources
import android.util.Log
import android.view.Gravity
import androidx.compose.ui.geometry.Offset
import com.funny.data_saver.core.DataSaverConverter.registerTypeConverters
import com.funny.translation.BaseApplication
import com.funny.translation.bean.UserBean
import com.funny.translation.codeeditor.ui.editor.EditorSchemes
import com.funny.translation.helper.JsonX
import com.funny.translation.sign.SignUtils
import com.funny.translation.translate.ui.thanks.SponsorSortType
import com.funny.translation.translate.utils.FunnyUncaughtExceptionHandler
import com.funny.translation.translate.utils.SortResultUtils
import com.hjq.toast.ToastUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class FunnyApplication : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        ctx = this
        FunnyUncaughtExceptionHandler.getInstance().init(ctx)
        ToastUtils.init(this)
        ToastUtils.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 260)

        GlobalScope.launch {
            initLanguageDisplay(resources)
            SignUtils.loadJs()
            SortResultUtils.init()
        }

        // For ComposeDataSaver
        registerTypeConverters<UserBean>(
            save = {
                JsonX.toJson(it)
            },
            restore = {
                // 修复旧版本 vip_start_time 为 Long 的问题
                try {
                    val matchResult = OLD_VIP_START_TIME.find(it)
                    if (matchResult != null)
                        JsonX.fromJson(it.replace(OLD_VIP_START_TIME, "$1:null"), UserBean::class)
                    else
                        JsonX.fromJson(it, UserBean::class)
                } catch (e: Exception){
                    e.printStackTrace()
                    UserBean()
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
    }

    companion object {
        var ctx by Delegates.notNull<FunnyApplication>()
        val resources: Resources get() = ctx.resources
        const val TAG = "FunnyApplication"
    }
}

val appCtx = FunnyApplication.ctx

private val OLD_VIP_START_TIME = """("vip_start_time"):(-?\d+)""".toRegex()