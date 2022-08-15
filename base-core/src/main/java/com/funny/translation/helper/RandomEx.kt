package com.funny.translation.helper

import kotlin.random.Random

/**
 * @author  FunnySaltyFish
 * @date    2022/8/2 16:08
 */

fun Random.randInt(from: Int, until: Int, k: Int = 1) : IntArray {
    var generated = 0
    val array = IntArray(k)
    while (generated < k){
        val t = this.nextInt(from, until)
        if (t !in array){
            array[generated++] = t
        }
    }
    return array
}

fun <T> Random.choice(col: List<T>) = col[this.nextInt(0, col.size)]
fun <T> Random.choice(col: List<T>, k:Int = 1): List<T>{
    check(k <= col.size){
        "k($k) must be less than or equal to the size of list(${col.size})"
    }
    val idx = randInt(0, col.size, k)
    return idx.map { i -> col[i] }
}

fun Random.nextFloat(min: Float, max: Float) = nextFloat() * (max - min) + min