package com.funny.translation.translate.ui.thanks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.funny.compose.loading.LoadingState
import com.funny.data_saver.core.mutableDataSaverListStateOf
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.get
import com.funny.translation.helper.string
import com.funny.translation.translate.R
import com.funny.translation.translate.database.TransHistoryBean
import com.funny.translation.translate.database.TransHistoryDao
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.findLanguageById
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class AnnualReportViewModel: ViewModel() {
    companion object{
        const val YEAR = 2023
        // 2023年全年，开始和结束对应的时间戳
        val START_TIME by lazy(LazyThreadSafetyMode.PUBLICATION) {
            LocalDateTime(YEAR, 1, 1, 0, 0).toInstant(TimeZone.currentSystemDefault())
                .toEpochMilliseconds()
        }

        val END_TIME by lazy(LazyThreadSafetyMode.PUBLICATION) {
            LocalDateTime(YEAR, 12, 31, 23, 59).toInstant(TimeZone.currentSystemDefault())
                .toEpochMilliseconds()
        }
    }
    var shouldLoadLatest = false

    var loadingState: LoadingState<Unit> = LoadingState.Loading
    var loadingDuration: Duration = Duration.ZERO

    var totalTranslateTimes by mutableStateOf( 0)
    var earliestTime by mutableStateOf( 0L)
    var latestTime by mutableStateOf( 0L)
    var totalTranslateWords by mutableStateOf( 0)
    var mostCommonSourceLanguage by mutableStateOf( "")
    var mostCommonTargetLanguage by mutableStateOf( "")
    var mostCommonSourceLanguageTimes by mutableStateOf( 0)
    var mostCommonTargetLanguageTimes by mutableStateOf( 0)
    var enginesUsesList by mutableDataSaverListStateOf(DataSaverUtils, "annual_report_engines_uses_list", listOf<Pair<String, Int>>())

    private val transHistoryDao: TransHistoryDao = appDB.transHistoryDao

    @OptIn(ExperimentalTime::class)
    suspend fun loadAnnualReport() = withContext(Dispatchers.IO){
        if (loadingState is LoadingState.Success) {
            return@withContext
        }
        if (loadingState is LoadingState.Loading) {
            loadingDuration = measureTime {
                // 从数据库中读取各种值
                var allHistories = transHistoryDao.queryAllBetween(START_TIME, END_TIME)
                if (allHistories.isEmpty()) {
                    allHistories = transHistoryDao.queryAllBetween(END_TIME, System.currentTimeMillis())
                    shouldLoadLatest = true
                }
                if (allHistories.isEmpty()) {
                    throw Exception(string(R.string.no_data))
                }
                totalTranslateTimes = allHistories.size
                val specialTimes = allHistories.findSpecialTimes()
                earliestTime = specialTimes.first
                latestTime = specialTimes.second
                totalTranslateWords = allHistories.sumOf { it.sourceString.length }
                val usesSourceLanguageMap = mutableMapOf<Int, Int>()
                val usesTargetLanguageMap = mutableMapOf<Int, Int>()
                val usesEngineMap = mutableMapOf<String, Int>()
                // 语言使用次数和引擎使用次数
                allHistories.forEach {
                    usesSourceLanguageMap[it.sourceLanguageId] =
                        usesSourceLanguageMap.get(it.sourceLanguageId, 0) + 1
                    usesTargetLanguageMap[it.targetLanguageId] =
                        usesTargetLanguageMap.get(it.targetLanguageId, 0) + 1
                    it.engineNames.forEach { engineName ->
                        usesEngineMap[engineName] = usesEngineMap.get(engineName, 0) + 1
                    }
                }
                // 计算最常用的语言
                val mostCommonSourceLanguageId = usesSourceLanguageMap.keyOfMaxValue()
                val mostCommonTargetLanguageId = usesTargetLanguageMap.keyOfMaxValue()
                mostCommonSourceLanguage =
                    findLanguageById(mostCommonSourceLanguageId).displayText
                mostCommonTargetLanguage =
                    findLanguageById(mostCommonTargetLanguageId).displayText
                mostCommonSourceLanguageTimes =
                    usesSourceLanguageMap[mostCommonSourceLanguageId] ?: 0
                mostCommonTargetLanguageTimes =
                    usesTargetLanguageMap[mostCommonTargetLanguageId] ?: 0
                // 计算引擎使用次数
                val engineUsesList = usesEngineMap.toList().sortedByDescending { it.second }
                enginesUsesList = engineUsesList
            }
            loadingState = LoadingState.Success(Unit)
        }
    }
}

// 根据历史记录，找到一天中的最早时间和最晚时间
private fun List<TransHistoryBean>.findSpecialTimes(): Pair<Long, Long> {
    // 计算出每一个对应的年月日、时分秒，找到时分秒最少的那一个
    var earliestTimeOfDay: Int = Int.MAX_VALUE
    var earliestTimeIndex = -1
    var latestTimeOfDay: Int = Int.MIN_VALUE
    var latestTimeIndex = -1
    this.forEachIndexed { index, transHistoryBean ->
        val localDateTime = Instant.fromEpochMilliseconds(transHistoryBean.time).toLocalDateTime(TimeZone.currentSystemDefault())
        val t = localDateTime.hour * 60 * 60 + localDateTime.minute * 60 + localDateTime.second
        if (t < earliestTimeOfDay) {
            earliestTimeOfDay = t
            earliestTimeIndex = index
        }
        if (t > latestTimeOfDay) {
            latestTimeOfDay = t
            latestTimeIndex = index
        }
    }
    return this[earliestTimeIndex].time to this[latestTimeIndex].time

}

private fun MutableMap<Int, Int>.keyOfMaxValue(): Int {
    var maxKey = -1
    var maxValue = Int.MIN_VALUE
    this.forEach { (key, value) ->
        if (value > maxValue) {
            maxKey = key
            maxValue = value
        }
    }
    return maxKey
}