package com.funny.translation.translate.ui.thanks

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.funny.compose.loading.loadingList
import com.funny.compose.loading.rememberRetryableLoadingState
import com.funny.jetsetting.core.ui.throttleClick
import com.funny.translation.helper.openUrl
import com.funny.translation.translate.R
import com.funny.translation.translate.bean.RecommendApp
import com.funny.translation.translate.ui.widget.CommonPage

suspend fun getRecommendApp(): List<RecommendApp> {
    val recommendAppList = listOf(
        RecommendApp(
            name = "Funny Translate",
            description = "Funny Translate is a translation tool that supports multiple languages and multiple translation engines. It is a translation tool that can be used offline and online. It is a translation tool that",
            iconUrl = "https://img.funnysaltyfish.fun/i/2022/09/02/63118ad144349.png",
            detailUrl = "https://www.funnysaltyfish.fun/trans"
        ),
        RecommendApp(
            name = "Funny Translate2",
            description = "Funny Translate is a translation tool that supports multiple languages and multiple translation engines. It is a translation tool that can be used offline and online. It is a translation tool that",
            iconUrl = "https://img.funnysaltyfish.fun/i/2022/09/02/63118ad144349.png",
            detailUrl = "https://www.funnysaltyfish.fun/trans"
        )
    )
    return recommendAppList
}

@Composable
fun AppRecommendationScreen() {
    val context = LocalContext.current
    CommonPage(title = stringResource(id = R.string.recommendation_app)) {
        val (recommendAppListState, retry) = rememberRetryableLoadingState(loader = ::getRecommendApp)
        LazyColumn() {
            loadingList(recommendAppListState, retry, key = { it.name }) { app ->
                ListItem(
                    headlineContent = { Text(app.name) },
                    supportingContent = { Text(app.description, overflow = TextOverflow.Ellipsis, maxLines = 2) },
                    leadingContent = {
                        SubcomposeAsyncImage(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),model = app.iconUrl, contentDescription = "icon") {
                            Image(painter = painter, contentDescription = contentDescription)
                        }
                    },
                    trailingContent = {
                        Icon(Icons.Default.ArrowRight, contentDescription = "detail")
                    },
                    modifier = Modifier.throttleClick {
                        context.openUrl(app.detailUrl)
                    }
                )
            }
        }
    }
}