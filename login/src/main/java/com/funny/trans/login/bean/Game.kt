package com.funny.trans.login.bean

import android.util.Log
import androidx.compose.runtime.*
import kotlin.math.roundToInt
import kotlin.math.sqrt
import com.funny.translation.helper.randInt
import com.funny.translation.helper.choice
import java.util.LinkedList
import kotlin.random.Random


sealed class GameStatus {
    object Waiting: GameStatus()
    class Playing(var type: Int): GameStatus()
    object Fail: GameStatus()
}

abstract class BaseGame {
    var width by mutableStateOf(0)
    var height by mutableStateOf(0)
    var status: GameStatus by mutableStateOf(GameStatus.Waiting)
    abstract val tipText: String
    abstract fun update(deltaTime: Int)
    abstract fun init()
}

interface IGameListener {
    fun onInput(char: Char)
    fun onFail()
    fun onDelete()
}

class MemoryNumberGame : BaseGame() {
    companion object {
        // 正在记忆
        const val PLAYING_TYPE_MEMORIZING = 0x1001
        // 数字模式：点击数字
        const val PLAYING_MODE_NUMBER = 0x1002
        // 字符模式
        const val PLAYING_MODE_CHAR = 0x1004
        const val DELETE_CHAR = '␡'

        private const val TAG = "MemoryNumberGame"
    }

    sealed class Block(var place: Pair<Int, Int>){
        var isShow:Boolean by mutableStateOf(true)
        class EmptyBlock(place: Pair<Int, Int>): Block(place)
        class NumberBlock(val number:Int, place: Pair<Int, Int>): Block(place)
        class CharacterBlock(val char: Char, place: Pair<Int, Int>): Block(place)
    }

    var rows by mutableStateOf(5)
    var cols by mutableStateOf(5)
    // 最大的数字，生成的范围为1..maxNumber
    private var maxNumber = 5
    // 一共几个block，包括空白的
    private var maxBlocks = 10
    val blocks = mutableListOf<Block>()

    private var isNumberMode = true
    private var retryTimes = 3
    private var difficulty = 0
        set(value) {
            if (value >= 10) field = 10
            else field = value
        }
    private var countDownTime by mutableStateOf(10000)
    private val needToClickNumbers = LinkedList<Int>()
    private var currentNeedToClickNumber by mutableStateOf(
        if(needToClickNumbers.isNotEmpty()) needToClickNumbers.toMutableStateList()[0]
        else -1
    )

    private val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ@_1234567890".toList()
    private val random = Random(System.currentTimeMillis())

    var gameListener: IGameListener? = null

    override fun init() {
        status = GameStatus.Waiting
        setParams()
    }

    override val tipText: String by derivedStateOf {
        when(status){
            is GameStatus.Waiting -> "点击按钮开始游戏~\n"
            is GameStatus.Playing -> when((status as GameStatus.Playing).type){
                PLAYING_TYPE_MEMORIZING -> "记忆中，倒计时：${countDownTime/1000}s\n"
                PLAYING_MODE_NUMBER -> "请点击【${currentNeedToClickNumber}】 倒计时：${countDownTime/1000}s\n${maxNumber-needToClickNumbers.size}/${maxNumber} 生命：${retryTimes}"
                PLAYING_MODE_CHAR -> "点击输入，空白结束~\n倒计时：${countDownTime/1000}s"
                else -> "\n"
            }
            else -> "\n"
        }
    }

    private fun setParams(){
        isNumberMode = random.nextBoolean()
        retryTimes = Math.max(4 - Math.ceil(difficulty.toFloat() * 0.4).toInt(), 1)
        rows = 5 + (difficulty * 0.4).roundToInt()
        cols = rows
        maxBlocks = Math.min((10 * sqrt(1+difficulty / 3.0)).toInt(), rows*cols*2/3)
        maxNumber = random.nextInt(maxBlocks/3, maxBlocks/2)
        countDownTime = maxNumber * 5000 - (difficulty/5*3000)
        Log.d(TAG, "setParams: difficulty:$difficulty maxBlocks:$maxBlocks maxNmber:$maxNumber")
    }

    // 继续，回到记忆状态
    fun restart(){
        status = GameStatus.Playing(PLAYING_TYPE_MEMORIZING)
        setParams()
        generateBlocks()
    }

    private fun fail(){
        status = GameStatus.Fail
        difficulty = 0
        gameListener?.onFail()
    }

    fun clickBlock(block: Block){
        if (status !is GameStatus.Playing) return
        val playing = status as GameStatus.Playing
        if (playing.type == PLAYING_TYPE_MEMORIZING) return

        if (playing.type == PLAYING_MODE_NUMBER){
            when(block){
                is Block.NumberBlock -> clickBlockNumber(block)
                else -> kotlin.run {
                    retryTimes--
                    if (retryTimes == 0) fail()
                }
            }
        }else if (playing.type == PLAYING_MODE_CHAR){
            when(block){
                is Block.CharacterBlock -> kotlin.run {
                    if (block.char == DELETE_CHAR) gameListener?.onDelete()
                    else gameListener?.onInput(block.char)
                    block.isShow = true
                }
                else -> kotlin.run {
                    difficulty++
                    restart()
                }
            }
        }
    }

    private fun clickBlockNumber(block: Block.NumberBlock){
        // 如果是第一个，说明点对了
        if(block.number == needToClickNumbers.first){
            block.isShow = true
            needToClickNumbers.removeFirst()
            if (needToClickNumbers.isEmpty()) {
                // 点完了，成功
                // 开始下一轮或结束
                difficulty++
                restart()
                return
            }
            currentNeedToClickNumber = needToClickNumbers.first
        }else{
            retryTimes--
            if (retryTimes == 0) {
                fail()
                return
            }
        }

    }

    private fun generateBlocks(){
        blocks.clear()
        // 先生成所有的block
        val allBlockPlaces = random.randInt(0, cols * rows, maxBlocks)
        if (isNumberMode){
            // 再挑几个idx，作为有数字的
            val numbersBlocksIdx = random.randInt(0, maxBlocks, maxNumber).sorted()
            var n = 0
            allBlockPlaces.forEachIndexed { i, place ->
                if (n >= maxNumber) return@forEachIndexed
                if (i == numbersBlocksIdx[n]){
                    blocks.add(Block.NumberBlock(++n, place / cols to place % cols))
                }else{
                    blocks.add(Block.EmptyBlock(place / cols to place % cols))
                }
            }
        }else{
            val randomChars = random.choice(chars, maxBlocks - 2)
            var j = 0
            allBlockPlaces.forEachIndexed { i, place ->
                if (i == 0) blocks.add(Block.EmptyBlock(place / cols to place % cols))
                else if (i == 1) blocks.add(
                    Block.CharacterBlock(
                        DELETE_CHAR,
                        place / cols to place % cols
                    )
                )
                else blocks.add(
                    Block.CharacterBlock(
                        randomChars[j++],
                        place / cols to place % cols
                    )
                )
            }
        }
    }

    private fun hideAll(){
        for (block in blocks) {
            block.isShow = false
        }
    }

    override fun update(deltaTime: Int) {
        if (status is GameStatus.Playing){
            countDownTime -= deltaTime
            val playingStatus = status as GameStatus.Playing
            when(playingStatus.type){
                PLAYING_TYPE_MEMORIZING -> kotlin.run {
                    if (countDownTime <= 0){
                        countDownTime = maxNumber * 5000 - (difficulty/5*3000)
                        hideAll()
                        if (isNumberMode){
                            needToClickNumbers.clear()
                            repeat(maxNumber){ i ->
                                needToClickNumbers.add(i+1)
                            }
                            if (difficulty >= 4) needToClickNumbers.shuffled()
                            currentNeedToClickNumber = needToClickNumbers.first
                            playingStatus.type = PLAYING_MODE_NUMBER
                        }else{
                            playingStatus.type = PLAYING_MODE_CHAR
                        }
                    }

                }

                PLAYING_MODE_NUMBER -> kotlin.run {
                    if (countDownTime <= 0){
                        fail()
                    }
                }

                PLAYING_MODE_CHAR -> kotlin.run {
                    if (countDownTime <= 0){
                        restart()
                    }
                }
            }
        }
    }
}