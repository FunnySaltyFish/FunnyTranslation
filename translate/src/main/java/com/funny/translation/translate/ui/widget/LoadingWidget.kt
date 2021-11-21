package com.funny.translation.translate.ui.widget

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import com.funny.translation.translate.R


private const val TAG = "LoadingWidget"
sealed class LoadingState<out R> {
    object Loading : LoadingState<Nothing>()
    data class Failure(val error : Throwable) : LoadingState<Nothing>()
    data class Success<T>(val data : T) : LoadingState<T>()

    val isLoading
        get() = this is Loading
    val isSuccess
        get() = this is Success<*>
}

@Composable
fun DefaultLoading() {
    CircularProgressIndicator()
}

@Composable
fun DefaultFailure(error: Throwable, retry : ()->Unit) {
    Text(text = stringResource(id = R.string.loading_error), modifier = Modifier.clickable(onClick = retry))
}

@Composable
fun <T> LoadingContent(
    modifier: Modifier = Modifier,
    loader : suspend ()->T,
    loading : @Composable ()->Unit = { DefaultLoading() },
    failure : @Composable (error : Throwable, retry : ()->Unit)->Unit = { error, retry->
        DefaultFailure(error, retry)
    },
    success : @Composable (data : T?)->Unit
) {
    var key by remember {
        mutableStateOf(false)
    }
    val state : LoadingState<T> by produceState<LoadingState<T>>(initialValue = LoadingState.Loading, key){
        value = try {
            Log.d(TAG, "LoadingContent: loading...")
            LoadingState.Success(loader())
        }catch (e: Exception){
            LoadingState.Failure(e)
        }
    }
    Box(modifier = modifier){
        when(state){
            is LoadingState.Loading -> loading()
            is LoadingState.Success<T> -> success((state as LoadingState.Success<T>).data)
            is LoadingState.Failure -> failure((state as LoadingState.Failure).error){
                key = !key
                Log.d(TAG, "LoadingContent: newKey:$key")
            }
        }
    }
}

fun <T> loadingState(
    scope : CoroutineScope = MainScope(),
    loader: suspend () -> T
): MutableState<LoadingState<T>> {
    val state : MutableState<LoadingState<T>> = mutableStateOf(LoadingState.Loading)
        scope.launch {
            try {
                val data = loader()
                state.value = LoadingState.Success(data)
            } catch(e : Exception) {
                state.value = LoadingState.Failure(e)
            }
        }
    return state
}