package com.funny.translation.helper

import android.content.Context
import android.os.Parcelable
import com.tencent.mmkv.MMKV
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface DataSaveInterface{
    fun <T> saveData(key:String, data : T)
    fun <T> readData(key: String, default : T) : T
}

class DataSaveMKKV<T : Any>(val key : String, val default: T) : DataSaveInterface, ReadWriteProperty<Any?,T> {
    lateinit var context : Context
    lateinit var _oldValue :T

    companion object {
        lateinit var kv : MMKV
        fun initKV(newKV : MMKV){
            kv = newKV
        }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return readData(key, default)
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
//        if (_oldValue == T) return
//        else saveData()
        saveData(key, value)
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