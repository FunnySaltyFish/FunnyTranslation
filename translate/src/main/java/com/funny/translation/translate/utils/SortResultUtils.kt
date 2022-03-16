package com.funny.translation.translate.utils

import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.translate.bean.Consts
import com.funny.translation.translate.database.DefaultData
import com.funny.translation.translate.database.appDB
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

typealias TranslationEngineName = String

object SortResultUtils {
    var mapping : HashMap<TranslationEngineName, Int> = hashMapOf()
    private val gson = GsonBuilder().enableComplexMapKeySerialization().create()
    var localEngines = listOf<TranslationEngineName>()
//    val engineComparator = Comparator<Int> { o1, o2 -> o1 - o2 }

    suspend fun init(){
        when(val json = DataSaverUtils.readData(Consts.KEY_SORT_RESULT,"")){
            "" -> initMapping(getLocalEngineNames())
            else -> withContext(Dispatchers.IO){
                readMapping(json)
            }
        }
    }

    suspend fun getLocalEngineNames() : List<TranslationEngineName> = withContext(Dispatchers.IO){
        if (localEngines.isEmpty()) {
            localEngines = (DefaultData.bindEngines.map { it.name } + appDB.jsDao.getAllJs().first().map { it.fileName })//.sortedBy { mapping.get(it, Int.MAX_VALUE) }
        }
        localEngines
    }

    private fun initMapping(engines : List<TranslationEngineName>){
        engines.mapIndexed { i, engine ->
            mapping[engine] = i
        }
    }

    private fun readMapping(json : String){
        val type = object : TypeToken<HashMap<TranslationEngineName, Int>>() {}.type
        mapping = gson.fromJson(json, type)
    }

    fun checkEquals(list : List<TranslationEngineName>) : Boolean = when{
        localEngines.size != list.size -> false
        else -> {
            var f = true
            for(i in localEngines.indices){
                if(localEngines[i] != list[i]){
                    f = false
                    break
                }
            }
            f
        }
    }

    fun resetMappingAndSave(list : List<TranslationEngineName>){
        initMapping(list)
        DataSaverUtils.saveData(Consts.KEY_SORT_RESULT ,gson.toJson(mapping))
    }

    fun <K,V> HashMap<K,V>.get(key: K, default: V) = try {
        get(key)
    }catch (e: Exception){
        default
    }
}