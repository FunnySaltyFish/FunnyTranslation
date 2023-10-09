package com.funny.translation.helper

import android.content.Context
import android.content.SharedPreferences
import com.funny.translation.Consts
import com.funny.translation.bean.AppLanguage
import java.util.Locale

object LocaleUtils {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var appLanguage: AppLanguage

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences("shared_pref", Context.MODE_PRIVATE)
    }

    fun getWarpedContext(context: Context, locale: Locale = getAppLanguage().toLocale()): Context {
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

    fun saveAppLanguage(appLanguage: AppLanguage) {
        this.appLanguage = appLanguage
        sharedPreferences.edit().putInt(Consts.KEY_APP_LANGUAGE, appLanguage.ordinal).apply()
    }

    fun getAppLanguage(): AppLanguage {
        if (!this::appLanguage.isInitialized) {
            appLanguage = kotlin.runCatching {
                AppLanguage.values()[sharedPreferences.getInt(Consts.KEY_APP_LANGUAGE, 0)]
            }.onFailure { it.printStackTrace() }.getOrDefault(AppLanguage.FOLLOW_SYSTEM)
        }
        return this.appLanguage
    }

    // 不经过获取 AppLanguage -> Locale 的过程，直接获取 Locale
    // 这个方法会在 attachBaseContext() 里调用，所以不能使用 AppLanguage 这个类
    fun getLocaleDirectly(): Locale {
        val id = sharedPreferences.getInt(Consts.KEY_APP_LANGUAGE, 0)
        return when(id) {
            0 -> Locale.getDefault()
            1 -> Locale.ENGLISH
            2 -> Locale.CHINESE
            else -> Locale.getDefault()
        }
    }
}