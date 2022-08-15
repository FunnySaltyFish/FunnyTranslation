package com.funny.translation.helper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async

/*
* 异步懒加载，by FunnySaltyFish
*
* @param T 要加载的数据类型
* @param scope 加载时的协程作用域
* @param block 加载代码跨
* @return Lazy<Deferred<T>>
*/
fun <T> lazyPromise(scope: CoroutineScope = MainScope(), block: suspend CoroutineScope.() -> T) =
    lazy {
        scope.async(start = CoroutineStart.LAZY) {
            block.invoke(this)
        }
    }