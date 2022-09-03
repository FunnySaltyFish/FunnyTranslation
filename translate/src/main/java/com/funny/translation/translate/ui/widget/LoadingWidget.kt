package com.funny.translation.translate.ui.widget

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
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
fun DefaultLoading(modifier: Modifier = Modifier) {
    CircularProgressIndicator(modifier)
}

@Composable
fun DefaultFailure(modifier: Modifier = Modifier, retry: () -> Unit) {
    Text(text = stringResource(id = R.string.loading_error), modifier = Modifier.clickable(onClick = retry))
}

/**
 * 通用加载微件
 * @author [FunnySaltyFish](https://blog.funnysaltyfish.fun/)
 * @param modifier Modifier 整个微件包围在Box中，此处修饰此Box
 * @param loader  加载函数，返回值为正常加载出的结果
 * @param loading 加载中显示的页面，默认为转圈圈
 * @param failure 加载失败显示的页面，默认为文本，点击可以重新加载（retry即为重新加载的函数）
 * @param success 加载成功后的页面，参数[T]即为返回的结果
 */
@Composable
fun <T,K> LoadingContent(
    modifier: Modifier = Modifier,
    key: K,
    updateKey: (K) -> Unit,
    loader : suspend ()->T,
    loading : @Composable ()->Unit = { DefaultLoading() },
    failure : @Composable (error : Throwable, retry : ()->Unit)->Unit = { error, retry->
        DefaultFailure(retry = retry)
    },
    success : @Composable BoxScope.(data : T)->Unit
) {
    val state : LoadingState<T> by produceState<LoadingState<T>>(initialValue = LoadingState.Loading, key){
        value = try {
            Log.d(TAG, "LoadingContent: loading...")
            LoadingState.Success(loader())
        }catch (e: Exception){
            e.printStackTrace()
            LoadingState.Failure(e)
        }
    }
    Box(modifier = modifier){
        when(state){
            is LoadingState.Loading -> loading()
            is LoadingState.Success<T> -> success((state as LoadingState.Success<T>).data)
            is LoadingState.Failure -> failure((state as LoadingState.Failure).error){
                updateKey(key)
            }
        }
    }
}

@Composable
fun <T> LoadingContent(
    modifier: Modifier = Modifier,
    loader : suspend ()->T,
    loading : @Composable ()->Unit = { DefaultLoading() },
    failure : @Composable (error : Throwable, retry : ()->Unit)->Unit = { error, retry->
        DefaultFailure(retry = retry)
    },
    success : @Composable BoxScope.(data : T)->Unit
) {
    var key by remember {
        mutableStateOf(false)
    }
    LoadingContent(modifier, key, { k -> key = !k }, loader, loading, failure, success)
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