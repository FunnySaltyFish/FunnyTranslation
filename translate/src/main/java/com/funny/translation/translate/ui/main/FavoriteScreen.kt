package com.funny.translation.translate.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.funny.translation.translate.*
import com.funny.translation.translate.R
import com.funny.translation.translate.database.TransFavoriteBean
import com.funny.translation.translate.ui.extension.items
import com.funny.translation.translate.ui.widget.CommonPage

@Composable
fun FavoriteScreen(
    modifier: Modifier = Modifier,
) {
    CommonPage(
        modifier = modifier,
        title = stringResource(id = R.string.favorite)
    ) {
        val navController = LocalNavController.current
        val vm: FavoriteViewModel = viewModel()
        TransFavoriteList(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            transFavorites = vm.transFavorites.collectAsLazyPagingItems(),
            onClickItem = { transFavoriteBean ->
                navController.navigateToTextTrans(
                    transFavoriteBean.sourceString,
                    findLanguageById(transFavoriteBean.sourceLanguageId),
                    findLanguageById(transFavoriteBean.targetLanguageId)
                )
            },
            onDeleteFavorite = vm::deleteTransFavorite
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TransFavoriteList(
    modifier: Modifier,
    transFavorites: LazyPagingItems<TransFavoriteBean>,
    onClickItem: (TransFavoriteBean) -> Unit,
    onDeleteFavorite: (TransFavoriteBean) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
    ) {
        if (transFavorites.itemCount == 0) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.no_favorite),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 18.sp
                    )
                }
            }
        }
        items(transFavorites, key = { it.id }) { transFavorite ->
            SwipeToDismissItem(modifier = Modifier.fillMaxWidth(), onDismissed = {
                onDeleteFavorite(transFavorite)
            }) {
                FavoriteItem(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .animateItemPlacement(),
                    item = transFavorite,
                    onClick = {
                        onClickItem(transFavorite)
                    }
                )
            }
        }
    }
}

@Composable
private fun FavoriteItem(
    modifier: Modifier,
    item: TransFavoriteBean,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(24.dp)
            )
            .clip(shape = RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(start = 20.dp, end = 20.dp, bottom = 16.dp, top = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = item.engineName,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.W500
            )
            SpeakButton(
                modifier = Modifier.offset(8.dp),
                text = item.resultText,
                language = findLanguageById(item.targetLanguageId)
            )
            CopyButton(
                text = item.resultText,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = item.sourceString.replace("\n", "  "),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.resultText.replace("\n", "  "),
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
            fontSize = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDismissItem(
    modifier: Modifier,
    onDismissed: () -> Unit,
    dismissContent: @Composable RowScope.() -> Unit
) {
    // 侧滑删除所需State
    val dismissState = rememberDismissState()
    // 按指定方向触发删除后的回调，在此处变更具体数据
    if (dismissState.isDismissed(DismissDirection.StartToEnd)) {
        onDismissed()
    }
    SwipeToDismiss(
        state = dismissState,
        modifier = modifier,
        // 允许滑动删除的方向
        directions = setOf(DismissDirection.StartToEnd),
        // "背景 "，即原来显示的内容被划走一部分时显示什么
        background = {
            val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    DismissValue.Default -> Color.LightGray
                    DismissValue.DismissedToEnd, DismissValue.DismissedToStart -> MaterialTheme.colorScheme.errorContainer
                }
            )
            val alignment = when (direction) {
                DismissDirection.StartToEnd -> Alignment.CenterStart
                DismissDirection.EndToStart -> Alignment.CenterEnd
            }
            val icon = Icons.Default.Delete
            val scale by animateFloatAsState(
                if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                Icon(
                    icon,
                    contentDescription = "Localized description",
                    modifier = Modifier.scale(scale)
                )
            }
        },
        dismissContent = dismissContent
    )
}