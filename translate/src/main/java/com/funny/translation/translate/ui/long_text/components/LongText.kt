package com.funny.translation.translate.ui.long_text.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString

/**
 * 基于 LazyColumn 实现的长文本组件
 * @param modifier Modifier
 * @param state LongTextState
 */
@Composable
fun LongText(
    modifier: Modifier = Modifier,
    state: LongTextState,
) {
    val textArray = state.textArray
    LazyColumn(
        modifier,
        state = state.lazyListState,
    ) {
        items(textArray.size) { index ->
            Text(
                text = textArray[index].second,
                onTextLayout = {
                    state.updateTextLayoutResult(index, it)
                }
            )
        }
    }
}

@Composable
fun LongText(
    modifier: Modifier = Modifier,
    text: AnnotatedString,
) {
    LongText(modifier, state = rememberLongTextState(text = text))
}


// 起始位置、对应的 AnnotatedString
typealias SplitText = Pair<Int, AnnotatedString>

class LongTextState(
    val text: AnnotatedString,
    val lazyListState: LazyListState = LazyListState()
) {
    val textArray: List<SplitText> = text.split("\n")

    private val textLayoutResults: Array<TextLayoutResult?> = arrayOfNulls(textArray.size)

    /**
     * 跳转到指定的位置
     * @param index Int **原文本中某个字符的位置**
     */
    suspend fun scrollToIndex(index: Int) {
        val itemIndex = textArray.binarySearchIndex { it.first.compareTo(index) }
//        Log.d(TAG, "scrollToIndex: index: $index, itemIndex: $itemIndex")
        if (itemIndex >= 0) {
            val layoutResult = textLayoutResults[itemIndex]
            // 如果没有 layoutResult，就直接跳转到对应的 item（因为没法计算出对应的 offset）
            // 事实上这是绝大部分情况，因为只有在 item 可见的时候才会有 layoutResult
            if (layoutResult == null) {
//                Log.d(TAG, "scrollToIndex: layoutResult == null")
                lazyListState.animateScrollToItem(itemIndex)
                return
            }
            // 所在子项的偏移位置（也就是原始位置 - 子项的起始位置）
            val itemTextOffset = index - textArray[itemIndex].first
            val scrollOffset = layoutResult.getLineForOffset(itemTextOffset).let {
                layoutResult.getLineTop(it)
            }
//            Log.d(TAG, "scrollToIndex: itemTextOffset: $itemTextOffset, scrollOffset: $scrollOffset")
            lazyListState.animateScrollToItem(itemIndex, scrollOffset.toInt())
        }
    }

    fun updateTextLayoutResult(index: Int, result: TextLayoutResult) {
        textLayoutResults[index] = result
    }

    companion object {
        private const val TAG = "LongTextState"
    }
}

@Composable
fun rememberLongTextState(text: AnnotatedString, lazyListState: LazyListState = rememberLazyListState()): LongTextState {
    return remember(text) {
        // lazyListState 不能放在 rememberLazyListState() 里面，否则会导致每次都会创建新的 LazyListState
        // 从而丢失滚动位置
        LongTextState(text = text, lazyListState = lazyListState)
    }
}

@Composable
fun rememberLongTextState(text: String, lazyListState: LazyListState = rememberLazyListState()) = rememberLongTextState(
    text = buildAnnotatedString {
        append(text)
    },
    lazyListState = lazyListState
)

fun AnnotatedString.split(delimiter: String): List<SplitText> {
    val result = mutableListOf<SplitText>()
    var start = 0
    while (true) {
        val index = this.indexOf(delimiter, start)
        if (index == -1) {
            result.add(start to this.subSequence(start, this.length))
            break
        }
        result.add(start to this.subSequence(start, index))
        start = index + delimiter.length
    }
    return result
}

// 二分查找，返回小于等于目标的 **index**
fun <T> List<T>.binarySearchIndex(
    fromIndex: Int = 0,
    toIndex: Int = size,
    comparison: (T) -> Int
): Int {
    var low = fromIndex
    var high = toIndex - 1
    while (low <= high) {
        val mid = (low + high).ushr(1)
        val midVal = this[mid]
        val cmp = comparison(midVal)
        when {
            cmp < 0 -> low = mid + 1
            cmp > 0 -> high = mid - 1
            else -> return mid // key found
        }
    }
    return high
}