package com.funny.translation.translate.ui.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.funny.translation.helper.ClipBoardUtil
import com.funny.translation.helper.SimpleAction
import com.funny.translation.translate.R
import com.funny.translation.translate.database.TransHistoryBean
import com.funny.translation.translate.findLanguageById
import com.funny.translation.translate.ui.extension.items
import com.funny.translation.translate.ui.widget.CommonNavBackIcon
import com.funny.translation.translate.ui.widget.CommonTopBar
import com.funny.translation.translate.ui.widget.UpperPartBackground
import com.funny.translation.ui.touchToScale


@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    progressProvider: () -> Float,
    navigateBackAction: SimpleAction
) {
    val vm: MainViewModel = viewModel()
    UpperPartBackground(
        modifier = modifier,
        cornerSizeProvider = { ((1 - progressProvider()) * 40).dp }
    ) {
        CommonTopBar(
            title = stringResource(id = R.string.history),
            navigationIcon = {
                CommonNavBackIcon(navigateBackAction = navigateBackAction)
            }
        )
        TransFavoriteList(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            transHistories = vm.transHistories.collectAsLazyPagingItems(),
            onClickHistory = { transHistory ->
                vm.translateText = transHistory.sourceString
                vm.sourceLanguage = findLanguageById(transHistory.sourceLanguageId)
                vm.targetLanguage = findLanguageById(transHistory.targetLanguageId)
                vm.translate()
            },
            onDeleteHistory = { sourceString ->
                vm.deleteTransHistory(sourceString)
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TransFavoriteList(
    modifier: Modifier,
    transHistories: LazyPagingItems<TransHistoryBean>,
    onClickHistory: (TransHistoryBean) -> Unit,
    onDeleteHistory: (String) -> Unit,
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = modifier,
        reverseLayout = true // 这一条使得最新的历史会在最下面
    ) {
        if (transHistories.itemSnapshotList.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.no_history), modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp), textAlign = TextAlign.Center
                )
            }
        } else {
            items(transHistories, key = { it.id }) { transHistory ->
                transHistory ?: return@items
                Row(
                    Modifier
                        .fillMaxWidth()
                        .touchToScale {
                            onClickHistory(transHistory)
                        }
                        .padding(start = 8.dp)
                        .animateItemPlacement(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = transHistory.sourceString,
                        fontWeight = FontWeight.W600,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 16.sp
                    )
                    Row {
                        IconButton(onClick = {
                            ClipBoardUtil.copy(
                                context,
                                transHistory.sourceString
                            )
                        }) {
                            Icon(
                                painterResource(id = R.drawable.ic_copy_content),
                                stringResource(R.string.copy_content)
                            )
                        }
                        IconButton(onClick = {
                            onDeleteHistory(transHistory.sourceString)
                        }) {
                            Icon(Icons.Default.Delete, stringResource(R.string.delete_this_history))
                        }
                    }
                }
            }
        }
    }
}