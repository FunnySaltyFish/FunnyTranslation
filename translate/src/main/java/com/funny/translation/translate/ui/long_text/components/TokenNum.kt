package com.funny.translation.translate.ui.long_text.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funny.compose.ai.token.TokenCounter
import com.funny.compose.loading.LoadingContent
import kotlinx.coroutines.delay

@Composable
internal fun RowScope.TokenNumRow(
    modifier: Modifier = Modifier,
    tokenCounter: TokenCounter,
    text: String,
) {
    Text(text = "Token: ")
    TokenNum(tokenCounter = tokenCounter, text = text)
}

@Composable
fun TokenNum(
    modifier: Modifier = Modifier,
    tokenCounter: TokenCounter,
    text: String,
) {
    LoadingContent(
        modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        loader = {
            // delay 500 以做 debounce
            delay(500)
            tokenCounter.count(text)
        },
        retryKey = tokenCounter to text,
        updateRetryKey = {},
        loading = {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        }
    ) { tokenNum ->
        Text(
            modifier = Modifier,
            text = tokenNum.toString()
        )
    }
}