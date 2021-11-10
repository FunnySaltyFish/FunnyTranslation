package com.funny.translation.translate.ui.thanks

import android.os.Build
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funny.translation.helper.readAssets
import com.funny.translation.translate.FunnyApplication
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class ThanksViewModel : ViewModel() {
    companion object {
        private val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd hh:mm:ss")
            .registerTypeAdapter(Date::class.java, object : JsonDeserializer<Date>{
                override fun deserialize(
                    json: JsonElement?,
                    typeOfT: Type?,
                    context: JsonDeserializationContext?
                ): Date {
                    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
                    return simpleDateFormat.parse(json?.asString?:"2021-01-01 00:00:00")!!
                }
            })  // yyyy-MM-dd HH:mm:ss
            .create()
        private const val TAG = "ThanksVM"
    }

    val sponsors by lazy {
        MutableLiveData(_sponsors)
    }

    private val _sponsors: List<Sponsor>
        get() {
            var sponsorList = listOf<Sponsor>()
            runBlocking {
                val json = getSponsorJson()
                withContext(Dispatchers.IO) {
                    val type = object : TypeToken<List<Sponsor>>() {}.type
                    sponsorList = gson.fromJson(json, type)
                    Log.d(TAG, "sponsorList: $sponsorList")
                }
                Log.d(TAG, "before return... ")

            }
            return sponsorList
        }

    private suspend fun getSponsorJson(): String {
        var s = ""
        withContext(Dispatchers.IO) {
            s = FunnyApplication.ctx.readAssets("sponsors.json")
        }
        return s
    }
}