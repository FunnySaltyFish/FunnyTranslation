package com.funny.translation.network

import com.funny.translation.BaseApplication
import com.funny.translation.helper.toastOnUi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend

const val CODE_SUCCESS = 50

@OptIn(ExperimentalContracts::class)
class Api<T>(
    private val func: KFunction<CommonData<T>?>,
    private val args: Array<out Any?>,
    private val dispatcher: CoroutineDispatcher
) {
    private var successFunc = { resp: CommonData<T> ->
        BaseApplication.ctx.toastOnUi(resp.message)
    }

    private var failFunc = { resp: CommonData<T> ->
        BaseApplication.ctx.toastOnUi(resp.error_msg ?: resp.message)
    }

    private var respNullFunc = {

    }

    private var errorFunc = { err: Throwable ->
        BaseApplication.ctx.toastOnUi(err.message)
    }

    fun success(block: (resp: CommonData<T>) -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        this.successFunc = block
    }

    fun addSuccess(block: (resp: CommonData<T>) -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        val old = this.successFunc
        this.successFunc = { resp ->
            old(resp)
            block(resp)
        }
    }

    fun fail(block: (resp: CommonData<T>) -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        this.failFunc = block
    }

    fun addFail(block: (resp: CommonData<T>) -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        val old = this.failFunc
        this.failFunc = { resp ->
            old(resp)
            block(resp)
        }
    }

    fun error(block: (Throwable) -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        this.errorFunc = block
    }

    fun addError(block: (Throwable) -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        val old = this.errorFunc
        this.errorFunc = { err ->
            old(err)
            block(err)
        }
    }

    suspend fun call(rethrowErr: Boolean = false): T? = withContext(dispatcher) {
        try {
            val resp = if (func.isSuspend) func.callSuspend(*args) else func.call(*args)
            if (resp == null) {
                withContext(Dispatchers.Main) {
                    respNullFunc()
                }
                return@withContext null
            }
            if (resp.code == CODE_SUCCESS) {
                withContext(Dispatchers.Main) {
                    successFunc(resp)
                }
            } else {
                withContext(Dispatchers.Main) {
                    failFunc(resp)
                }
            }
            resp.data
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                errorFunc(e)
            }
            e.printStackTrace()
            if (rethrowErr) throw e
            null
        }
    }
}

inline fun <reified T : Any?> apiNoCall(
    func: KFunction<CommonData<T>?>,
    vararg args: Any?,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    block: Api<T>.() -> Unit = {},
) = Api(func, args = args, dispatcher).apply(block)

suspend inline fun <reified T : Any?> api(
    func: KFunction<CommonData<T>?>,
    vararg args: Any?,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    rethrowErr: Boolean = false,
    block: Api<T>.() -> Unit = {},
) = apiNoCall(func, *args, dispatcher = dispatcher, block = block).call(rethrowErr)

//suspend inline fun <reified T> api(
//    noinline func: (args: Array<out Any>) -> CommonData<T>?,
//    vararg args: Any?,
//    dispatcher: CoroutineDispatcher = Dispatchers.IO,
//    block: Api<T>.() -> Unit = {},
//) {
//    // Api(func = ::func, args = args, dispatcher = dispatcher).apply(block).call()
//}