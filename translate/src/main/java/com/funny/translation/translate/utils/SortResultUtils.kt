package com.funny.translation.translate.utils

import com.funny.translation.Consts
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.JsonX
import com.funny.translation.translate.TranslationEngine
import com.funny.translation.translate.TranslationResult
import com.funny.translation.translate.database.DefaultData
import com.funny.translation.translate.database.appDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

typealias TranslationEngineName = String

object SortResultUtils {
    var mapping : HashMap<TranslationEngineName, Int> = hashMapOf()

    var localEngines = MutableStateFlow(listOf<TranslationEngineName>())
    private val defaultSort : (String)->Int = { mapping.get(it, Int.MAX_VALUE) }
    val defaultEngineSort : (TranslationEngine)->Int = { mapping.get(it.name, Int.MAX_VALUE) }
    val defaultResultSort : (TranslationResult)->Int = { mapping.get(it.engineName, Int.MAX_VALUE) }
//    val engineComparator = Comparator<Int> { o1, o2 -> o1 - o2 }

    suspend fun init(){
        localEngines.value = (DefaultData.bindEngines.map { it.name } + appDB.jsDao.getAllJs().first().map { it.fileName })
        when(val json = DataSaverUtils.readData(Consts.KEY_SORT_RESULT,"")){
            "" -> initMapping(localEngines.value)
            else -> withContext(Dispatchers.IO){
                readMapping(json)
            }
        }
    }

    private fun initMapping(engines : List<TranslationEngineName>){
        engines.mapIndexed { i, engine ->
            mapping[engine] = i
        }
    }

    private fun readMapping(json : String){
        mapping = JsonX.fromJson(json)
    }

    fun checkEquals(list : List<TranslationEngineName>) : Boolean = when {
        localEngines.value.size != list.size -> false
        else -> {
            var f = true
            for(i in localEngines.value.indices){
                if(localEngines.value[i] != list[i]){
                    f = false
                    break
                }
            }
            f
        }
    }

    fun resetMappingAndSave(list : List<TranslationEngineName>){
        initMapping(list)
        localEngines.value = localEngines.value.sortedBy(defaultSort)
        DataSaverUtils.saveData(Consts.KEY_SORT_RESULT, JsonX.toJson(mapping))
    }

    fun addNew(name : TranslationEngineName){
        mapping[name] = mapping.maxOf { it.value } + 1 // 默认排最后一个
        localEngines.value = localEngines.value + name
        DataSaverUtils.saveData(Consts.KEY_SORT_RESULT, JsonX.toJson(mapping))
    }

    fun remove(name : TranslationEngineName){
        mapping.remove(name)
        localEngines.value = localEngines.value - name
        DataSaverUtils.saveData(Consts.KEY_SORT_RESULT, JsonX.toJson(mapping))
    }

    fun <K,V> HashMap<K,V>.get(key: K, default: V) = try {
        get(key) ?: default
    } catch (e: Exception){
        default
    }
}