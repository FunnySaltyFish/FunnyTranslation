package com.funny.translation.translate.ui.long_text

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.funny.translation.debug.rememberStateOf
import com.funny.translation.helper.SimpleAction
import com.funny.translation.helper.string
import com.funny.translation.translate.LocalSnackbarState
import com.funny.translation.translate.R
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.showSnackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ColumnScope.CorpusListPart(
    vm: LongTextTransViewModel
) {
    Category(title = stringResource(id = R.string.corpus), expandable = false) {
        val pagerState = rememberPagerState(pageCount = { 2 })
        val scope = rememberCoroutineScope()
        fun changePage(index: Int) = scope.launch {
            pagerState.animateScrollToPage(index)
        }
        TabRow(
            pagerState.currentPage,
            modifier = Modifier.background(Color.Transparent),
            containerColor = Color.Transparent
        ) {
            Tab(pagerState.currentPage == 0, onClick = { changePage(0) }) {
                Text(
                    stringResource(R.string.current_corpus),
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Tab(pagerState.currentPage == 1, onClick = { changePage(1) }) {
                Text(
                    stringResource(R.string.all_corpus),
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalPager(
            modifier = Modifier
                .fillMaxWidth(),
            state = pagerState,
            verticalAlignment = Alignment.Top
        ) { page ->
            when(page) {
                0 -> {
                    CurrentCorpusList(vm = vm)
                }
                1 -> {
                    AllCorpusList(vm = vm)
                }
            }
        }
    }
}

@Composable
internal fun AllCorpusList(
    vm: LongTextTransViewModel,
) {
    val corpus = vm.allCorpus
    CorpusList(
        modifier = Modifier.heightIn(0.dp, 300.dp),
        corpus = corpus.list,
        addTerm = { corpus.add(it, alert = true) },
        removeTerm = corpus::remove,
        modifyTerm = { origin, target ->
            corpus.modify(origin, target, alert = true)
        },
        onDialogShowUpdate = vm::updateEditingTermState
    )
}

@Composable
internal fun CurrentCorpusList(
    vm: LongTextTransViewModel,
) {
    val corpus = vm.currentCorpus
    val allCorpus = vm.allCorpus
    val snackbar = LocalSnackbarState.current
    val scope = rememberCoroutineScope()
    val askForOther = remember {
        AskForModifyingOther(scope, snackbar)
    }
    CorpusList(
        modifier = Modifier.heightIn(0.dp, 300.dp),
        corpus = corpus.list,
        addTerm = {
            corpus.add(it, alert = true)
            allCorpus.add(it, alert = true)
        },
        removeTerm = {
            corpus.remove(it)
            askForOther.ask(
                message = string(R.string.message_remove_from_all_corpus),
                action = {
                    allCorpus.remove(it)
                }
            )
        },
        modifyTerm = { origin, target ->
            corpus.modify(origin, target, alert = true)
            askForOther.ask(
                message = string(R.string.message_modify_all_corpus),
                action = {
                    allCorpus.modify(origin, target, alert = true)
                }
            )
        },
        onDialogShowUpdate = vm::updateEditingTermState
    )
}

private class AskForModifyingOther(
    val coroutineScope: CoroutineScope,
    val snackHostState: SnackbarHostState
) {
    fun ask(message: String, action: SimpleAction) {
        coroutineScope.launch {
            snackHostState.showSnackbar(
                message = message,
                actionLabel = string(R.string.message_confirm),
                onClick = action
            )
        }
    }
}

@Composable
internal fun CorpusList(
    modifier: Modifier = Modifier,
    corpus: List<Term>,
    addTerm: (Term) -> Unit,
    removeTerm: (Term) -> Unit,
    modifyTerm: (Term, Term) -> Unit,
    onDialogShowUpdate: (Boolean) -> Unit = {}
) {
    LazyColumn(modifier) {
        items(corpus, key = { it.first }) { term ->
            var showEditDialog by rememberStateOf(value = false)
            var source by rememberStateOf(value = term.first)
            var target by rememberStateOf(value = term.second)
            TermDialog(
                show = showEditDialog,
                updateShow = {
                    showEditDialog = it
                    onDialogShowUpdate(it)
                },
                source = source,
                updateSource = { source = it },
                target = target,
                updateTarget = { target = it }
            ) {
                modifyTerm(term, source to target)
            }

            ListItem(
                modifier = Modifier.clickable {
                    showEditDialog = true
                    onDialogShowUpdate(true)
                },
                headlineContent = {
                    Text(text = term.first)
                },
                supportingContent = {
                    Text(text = term.second)
                },
                trailingContent = {
                    IconButton(onClick = { removeTerm(term) }) {
                        FixedSizeIcon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            )
        }
        item {
            // 添加
            var source by rememberStateOf(value = "")
            var target by rememberStateOf(value = "")
            var show by rememberStateOf(value = false)
            TermDialog(
                show = show,
                updateShow = {
                    show = it
                    onDialogShowUpdate(it)
                },
                source = source,
                updateSource = { source = it },
                target = target,
                updateTarget = { target = it }
            ) {
                addTerm(source to target)
            }
            IconButton(onClick = {
                show = true
                onDialogShowUpdate(true)
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier)
            }
        }
    }
}

@Composable
private fun TermDialog(
    show: Boolean,
    updateShow: (Boolean) -> Unit,
    source: String,
    updateSource: (String) -> Unit,
    target: String,
    updateTarget: (String) -> Unit,
    confirmAction: SimpleAction
) {
    if (show) {
        val isSourceErr by remember(source) { derivedStateOf { source.isBlank() } }
        val isTargetErr by remember(target) { derivedStateOf { target.isBlank() } }

        AlertDialog(
            onDismissRequest = { updateShow(false) },
            confirmButton = {
                TextButton(onClick = {
                    confirmAction()
                    updateShow(false)
                }, enabled = !isSourceErr && !isTargetErr) {
                    Text(text = stringResource(id = R.string.message_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { updateShow(false) }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            },
            text = {
                // Column, 两个 TextField
                Column {
                    TextField(
                        value = source,
                        onValueChange = updateSource,
                        label = {
                            Text(text = stringResource(id = R.string.source_text))
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        singleLine = true,
                        isError = isSourceErr
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = target,
                        onValueChange = updateTarget,
                        label = {
                            Text(text = stringResource(id = R.string.target_text))
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        isError = isTargetErr
                    )
                }
            }
        )
    }
}

