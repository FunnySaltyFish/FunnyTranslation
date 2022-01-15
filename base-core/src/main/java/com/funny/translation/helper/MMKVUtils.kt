package com.funny.translation.helper

import com.tencent.mmkv.MMKV

object MMKVUtils {
     val kv: MMKV by lazy {
        MMKV.defaultMMKV()
    }
}