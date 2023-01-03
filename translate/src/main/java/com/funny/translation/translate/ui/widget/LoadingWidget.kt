package com.funny.translation.translate.ui.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.paging.*
import com.funny.translation.translate.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


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
    CircularProgressIndicator(modifier.fillMaxWidth().wrapContentWidth())
}

@Composable
fun DefaultFailure(modifier: Modifier = Modifier, retry: () -> Unit) {
    Text(text = stringResource(id = R.string.loading_error), modifier = modifier
        .clickable(onClick = retry)
        .fillMaxWidth()
        .wrapContentWidth(CenterHorizontally))
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
    val state : LoadingState<T> by rememberRetryableLoadingState(loader = loader, key = key)
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


fun <T : Any> LazyListScope.loadingList(
    value: State<LoadingState<List<T>>>,
    retry: () -> Unit,
    key : ((T) -> Any)?,
    loading : @Composable ()->Unit = { DefaultLoading() },
    failure : @Composable (error : Throwable, retry : ()->Unit) -> Unit = { err, re->
        DefaultFailure(retry = re)
    },
    success : @Composable (data : T)->Unit,
){
    when(value.value){
        is LoadingState.Loading -> item(key = "loading"){ loading() }
        is LoadingState.Success<*> -> items((value.value as LoadingState.Success<List<T>>).data, key){
            success(it)
        }
        is LoadingState.Failure -> item { failure((value.value as LoadingState.Failure).error, retry) }
    }
}

/**
 * 可重试的 LoadingState，由于 Key 内部持有，额外返回一个修改 Key 的函数
 * @param initialValue LoadingState<T> 初始加载状态，默认为 [LoadingState.Loading]
 * @param loader 加载值的函数
 * @return Pair<State<LoadingState<T>>, () -> Unit>
 */
@Composable
fun <T> rememberRetryableLoadingState(
    initialValue: LoadingState<T> = LoadingState.Loading,
    loader: suspend () -> T,
): Pair<State<LoadingState<T>>, () -> Unit> {
    var key by remember {
        mutableStateOf(false)
    }
    val update = remember {
        {
            key = !key
        }
    }
    val loadingState: State<LoadingState<T>> = rememberRetryableLoadingState(key = key, loader = loader, initialValue = initialValue)
    return (loadingState to update)
}

@Composable
fun <T, K> rememberRetryableLoadingState(
    initialValue: LoadingState<T> = LoadingState.Loading,
    key: K,
    loader: suspend () -> T,
): State<LoadingState<T>> {
    val loadingState: State<LoadingState<T>> = produceState(initialValue = initialValue, key) {
        value = try {
            LoadingState.Success(loader())
        }catch (e: Exception){
            e.printStackTrace()
            LoadingState.Failure(e)
        }
    }
    return loadingState
}