package com.funny.translation.codeeditor.base

import android.app.Application
import android.content.Context
import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funny.translation.helper.coroutine.Coroutine
import com.funny.translation.helper.toastOnUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

open class BaseViewModel(val application: Application) : ViewModel() {

    val context: Context by lazy { application }

    fun <T> execute(
        scope: CoroutineScope = viewModelScope,
        context: CoroutineContext = Dispatchers.IO,
        block: suspend CoroutineScope.() -> T
    ): Coroutine<T> {
        return Coroutine.async(scope, context) { block() }
    }

    fun <R> submit(
        scope: CoroutineScope = viewModelScope,
        context: CoroutineContext = Dispatchers.IO,
        block: suspend CoroutineScope.() -> Deferred<R>
    ): Coroutine<R> {
        return Coroutine.async(scope, context) { block().await() }
    }

    @CallSuper
    override fun onCleared() {
        super.onCleared()
    }

    open fun toastOnUi(message: Int) {
        context.toastOnUi(message)
    }

    open fun toastOnUi(message: CharSequence?) {
        context.toastOnUi(message ?: toString())
    }

}