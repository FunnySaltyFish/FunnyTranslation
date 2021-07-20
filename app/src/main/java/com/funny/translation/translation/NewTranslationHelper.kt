package com.funny.translation.translation

import android.content.Context
import android.util.Log
import com.funny.translation.bean.Consts
import kotlinx.coroutines.*
import org.json.JSONException
import java.lang.Exception
import java.util.*

class NewTranslationHelper(private var tasks: ArrayList<BasicTranslationTask>) {

    private lateinit var lastFinishTime: MutableMap<Short, Long>
    private lateinit var translationJob: Job

    var mode : Short = 0
    var totalTimes = 0

    private var progress = 0

    companion object {
        val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        fun getSleepTime(engineKind: Short) : Long = when (engineKind) {
            Consts.ENGINE_BAIDU_NORMAL -> Consts.BAIDU_SLEEP_TIME
            else -> 50
        }

    }

    @ExperimentalCoroutinesApi
    fun translate(
            listener: OnTaskFinishListener
    ) {
        lastFinishTime = mutableMapOf()
        tasks.forEach {
            if (!lastFinishTime.containsKey(it.engineKind)) {
                lastFinishTime[it.engineKind] = 0
            }
        }
        val futureTasks: Stack<Deferred<TranslationResult>> = Stack()
        val errorHandler = CoroutineExceptionHandler { _, exception ->
            listener.onFailure("翻译过程产生错误：$exception")
        }

        var id = 0
        translationJob = scope.launch {
            withContext(Dispatchers.IO) {
                //在这里加能够并行执行，前面的错误走ExceptionHandler，但是最后会有一个报错，表示整体发生过错误
                supervisorScope {
                    while (id < tasks.size) {
                        val curTask = tasks[id]
                        id++
                        val curTime = System.currentTimeMillis()
                        if ((curTime - lastFinishTime.getValue(curTask.engineKind)) >=
                                getSleepTime(curTask.engineKind)
                        ) {
                            //try {
                            //直接在这里加sScope能走errorHandler，但是只能顺序进行
                            //supervisorScope {
                            //在async加suScope可以实现异常进入下面的handler，但是只能顺序执行
                            futureTasks.add(async(errorHandler) {
                                curTask.translate(mode)
                                val result = curTask.getResult()
                                //channel.send(result)
                                result
                            }.also { result ->
                                result.invokeOnCompletion { handler ->
                                    progress++
                                    Log.d("NewTranslationHelper", "translate: finish one")
                                    when (handler) {
                                        null -> listener.onSuccess(result.getCompleted())
                                        is CancellationException -> listener.onFailure("被取消了")
                                        else -> {
                                            listener.onFailure(if (handler is JSONException) handler.toString() else handler.message
                                                    ?: handler.toString())
                                        }
                                    }
                                }
                            })
                            //}
//                            } catch (e: Exception) {
//                                failureCall(TranslationResult("错误"))
//                            }
                            lastFinishTime[curTask.engineKind] = System.currentTimeMillis()
                            println("finish create${id}")
                        }
                        delay(100)
                    }
                }

            }

            try {
                futureTasks.forEach(){
                    it.await()
                }
            } catch (e: Exception) {
                println("在整个翻译过程中发生过异常 ： $e")
                //e.printStackTrace()
            }

            println("finish all tasks")
        }

    }

    fun getProgress() = progress

    fun isTranslating() = progress > 0 && progress < tasks.size

    fun cancelTask() : Unit {
        translationJob.cancel()
    }
}

interface OnTaskFinishListener{
    fun onSuccess(result: TranslationResult)
    fun onFailure(errorMsg : String)
}

@ExperimentalCoroutinesApi
fun main() {
//    val tasks: ArrayList<BasicTranslationTask> = arrayListOf()
//    repeat(7) {
//        tasks.add(BasicTranslationTask(0))
//        tasks.add(BasicTranslationTask(1))
//        //tasks.add(BasicTranslationTask(0))
//    }
//    val translationHelper = NewTranslationHelper(tasks)
//    val successCall: (TranslationResult) -> Unit = {
//        println(it)
//    }
//    val failureCall: (TranslationResult) -> Unit = {
//        println(it)
//    }
//    translationHelper.translate(successCall, failureCall)

}