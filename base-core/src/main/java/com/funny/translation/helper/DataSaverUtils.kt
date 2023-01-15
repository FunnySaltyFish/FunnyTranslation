package com.funny.translation.helper

import com.funny.data_saver.core.DataSaverInterface
import com.funny.data_saver_mmkv.DefaultDataSaverMMKV

// See https://github.com/FunnySaltyFish/ComposeDataSaver | 在 Jetpack Compose 中优雅完成数据持久化
val DataSaverUtils : DataSaverInterface = DefaultDataSaverMMKV