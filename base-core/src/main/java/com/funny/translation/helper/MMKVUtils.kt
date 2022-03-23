package com.funny.translation.helper

import android.os.Parcelable
import com.funny.data_saver.core.DataSaverInterface
import com.tencent.mmkv.MMKV

object MMKVUtils : DataSaverInterface {
     val kv: MMKV by lazy {
        MMKV.defaultMMKV()
    }

    override fun <T> saveData(key: String, data: T) {
        with(kv){
            when(data){
                is Long -> encode(key, data)
                is Int -> encode(key, data)
                is String -> encode(key, data)
                is Boolean -> encode(key, data)
                is Float -> encode(key, data)
                is Double -> encode(key, data)
                is Parcelable -> encode(key, data)
                is ByteArray -> encode(key, data)
                else -> throw IllegalArgumentException("This type of data is not supported!")
            }
        }
    }

    override fun <T> readData(key: String, default: T): T = with(kv){
        val res : Any =  when(default){
            is Long -> decodeLong(key, default)
            is Int -> decodeInt(key, default)
            is String -> decodeString(key, default)!!
            is Boolean -> decodeBool(key, default)
            is Float -> decodeFloat(key, default)
            is Double -> decodeDouble(key, default)
            else -> throw IllegalArgumentException("This type of data is not supported!")
        }
        return@with res as T
    }
}