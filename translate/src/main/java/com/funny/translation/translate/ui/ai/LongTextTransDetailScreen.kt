package com.funny.translation.translate.ui.ai

import android.net.Uri
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.funny.translation.translate.R
import com.funny.translation.translate.ui.main.LanguageSelectRow
import com.funny.translation.translate.ui.widget.CommonPage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.util.UUID

@Composable
fun LongTextTransDetailScreen(
    id: String = UUID.randomUUID().toString(),
    totalLength: Int = 0,
    inputFileUri: Uri
) {
    val vm: LongTextTransViewModel = viewModel()
    CommonPage(
        title = stringResource(id = R.string.long_text_trans),
        actions = {
            Row {
                TranslateButton(progress = vm.progress, onClick = vm::startTranslate )
            }
        }
    ) {
        // 传入参数时，先初始化各类型
        LaunchedEffect(key1 = id){
            vm.initArgs(id, totalLength, inputFileUri)
        }

        // 翻译结果
        Text(text = vm.resultText)

        // 当前术语
        AllCorpusList(
            modifier = Modifier.heightIn(0.dp, 300.dp),
            corpus = vm.allCorpus.toImmutableList(),
            addTerm = vm::addTerm,
            removeTerm = vm::removeTerm,
            modifyTerm = vm::modifyTerm
        )

        FunctionRow(modifier = Modifier.fillMaxSize().wrapContentHeight(Alignment.Bottom), vm = vm)
    }
}

@Composable
private fun AllCorpusList(
    modifier: Modifier = Modifier,
    corpus: ImmutableList<Term>,
    addTerm: (Term) -> Unit,
    removeTerm: (Term) -> Unit,
    modifyTerm: (Term, Term) -> Unit,
) {
    LazyColumn(modifier) {
        items(corpus, key = { it.first }) { term ->
            ListItem(
                headlineContent = {
                    Text(text = term.first)
                },
                trailingContent = {
                    Text(text = term.second)
                }
            )
        }
    }
}

@Composable
private fun TranslateButton(
    progress: Float = 1f,
    isTranslating: Boolean = false,
    onClick: () -> Unit
) {
    val borderColor = MaterialTheme.colorScheme.secondary
    val density = LocalDensity.current
    val size48dp = remember { with(density) { 48.dp.toPx() } }
    val size12dp = remember { with(density) { 12.dp.toPx() } }

    IconButton(
        modifier =
        Modifier.drawBehind {
            if (progress < 1f) drawArc(
                borderColor,
                startAngle = -90f,
                360f * progress,
                false,
                style = Stroke(width = 4f),
                topLeft = Offset(size12dp / 2, size12dp / 2),
                size = size.copy(size48dp - size12dp, size48dp - size12dp)
            )
        }, onClick = onClick
    ) {
        if (!isTranslating) Icon(
            Icons.Default.Done,
            contentDescription = stringResource(R.string.start_translate),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        else Icon(
            painter = painterResource(id = R.drawable.ic_pause),
            contentDescription = stringResource(R.string.stop_translate),
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }

}

@Composable
private fun FunctionRow(modifier: Modifier, vm: LongTextTransViewModel) {
//    Row(modifier) {
        LanguageSelectRow(
            modifier = modifier,
            sourceLanguage = vm.sourceLanguage,
            updateSourceLanguage = vm::updateSourceLanguage,
            targetLanguage = vm.targetLanguage,
            updateTargetLanguage = vm::updateTargetLanguage
        )
//    }
}

