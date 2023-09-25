package com.funny.translation.debug

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

/**
 * equals to remember { mutableXxxStateOf(value) }
 * @param value T
 * @return MutableState<out Any?>
 */
@Composable
inline fun <reified T> rememberStateOf(value: T): MutableState<T> = remember {
    when (value) {
        is Int -> mutableIntStateOf(value)
        is Float -> mutableFloatStateOf(value)
        is Double -> mutableDoubleStateOf(value)
        is Long -> mutableLongStateOf(value)
        else -> mutableStateOf(value)
    } as MutableState<T>
}