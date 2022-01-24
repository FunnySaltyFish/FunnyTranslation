package com.funny.jetsetting.core

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.currentCompositionLocalContext
import androidx.compose.runtime.staticCompositionLocalOf

interface DataSaveInterface{
    fun <T> saveData(key:String, data : T)
    fun <T> readData(key: String, default : T) : T
}

class DataSavePreferences : DataSaveInterface {
    companion object {
        lateinit var preference: SharedPreferences

        fun setContext(context: Context) {
            preference = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        }
    }


    private fun <T> getPreference(name: String, default: T): T = with(preference) {
        val res: Any = when (default) {
            is Long -> getLong(name, default)
            is String -> this.getString(name, default)!!
            is Int -> getInt(name, default)
            is Boolean -> getBoolean(name, default)
            is Float -> getFloat(name, default)
            else -> throw IllegalArgumentException("This type can be get from Preferences")
//            else -> deSerialization(getString(name,serialize(default)).toString())
        }
        return res as T
    }

    private fun <T> putPreference(name: String, value: T) = with(preference.edit()) {
        when (value) {
            is Long -> putLong(name, value)
            is Int -> putInt(name, value)
            is String -> putString(name, value)
            is Boolean -> putBoolean(name, value)
            is Float -> putFloat(name, value)
            else -> throw IllegalArgumentException("This type can be saved into Preferences")
        }.apply()
    }

    override fun <T> saveData(key: String, data: T) {
        putPreference(key, data)
    }

    override fun <T> readData(key: String, default: T): T = getPreference(key, default)
}

var LocalDataSave : ProvidableCompositionLocal<DataSaveInterface> = staticCompositionLocalOf {
    DataSavePreferences()
}