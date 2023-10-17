package com.funny.translation.translate.ui.long_text

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.funny.translation.debug.rememberStateOf
import com.funny.translation.translate.R

internal class EditablePrompt(var prefix: String, val suffix: String) {
    fun toPrompt(): String {
        return prefix + suffix
    }
}

@Composable
internal fun ColumnScope.PromptPart(
    initialPrompt: EditablePrompt,
    onPrefixUpdate: (String) -> Unit
) {
    Category(title = stringResource(id = R.string.task_prompt)) { expanded: Boolean ->
        Column {
            var prefix by rememberStateOf(initialPrompt.prefix)
            var textLayoutResult: TextLayoutResult? by rememberStateOf(null)
            BasicTextField(
                value = prefix,
                onValueChange = {
                    prefix = it
                    onPrefixUpdate(it)
                },
                onTextLayout = { result ->
                    textLayoutResult = result
                }
            ) { innerTextField ->
                innerTextField()
            }
            // 根据 textLayoutResult 计算出 prefix 的位置，
            // 实现 suffix 刚刚好接在 prefix 后面
            val maxLines by animateIntAsState(targetValue = if (expanded) 10 else 2, label = "PromptSuffix")
            Text(
                text = initialPrompt.suffix,
                modifier = Modifier.padding(top = 2.dp).animateContentSize(),
                style = LocalTextStyle.current.copy(color = Color.Gray),
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}