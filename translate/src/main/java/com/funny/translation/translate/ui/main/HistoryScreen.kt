package com.funny.translation.translate.ui.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.funny.translation.helper.ClipBoardUtil
import com.funny.translation.translate.R
import com.funny.translation.translate.database.TransHistoryBean
import com.funny.translation.translate.findLanguageById
import com.funny.translation.translate.ui.widget.UpperPartBackground
import com.funny.translation.ui.touchToScale

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    navigateBackAction: SimpleAction
) {
    val vm: MainViewModel = viewModel()
    UpperPartBackground(modifier = modifier) {
        FavoriteTopBar(navigateBackAction)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoriteTopBar(
    navigateBackAction: SimpleAction
) {
    TopAppBar(
        title = {
            Text(text = "历史记录")
        },
        navigationIcon = {
            IconButton(onClick = navigateBackAction) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = stringResource(id = R.string.back)
                )
            }
        }
    )
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
        modifier = modifier, reverseLayout = true // 这一条使得最新的历史会在最下面
    ) {
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
                        Icon(Icons.Default.Delete, "删除此历史记录")
                    }
                }
            }
        }
    }
}