package com.funny.translation.translate.ui.thanks

import android.content.Intent
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.W800
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.funny.translation.translate.R
import com.funny.translation.translate.WebViewActivity
import com.funny.translation.translate.ui.widget.DefaultFailure
import com.funny.translation.translate.ui.widget.DefaultLoading

@Composable
fun ThanksScreen() {
    val vm : ThanksViewModel = viewModel()
    val sponsors = vm.sponsors.collectAsLazyPagingItems()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
        ,
        horizontalAlignment = Alignment.Start
    ){
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = stringResource(id = R.string.thanks), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
            }
        }

        item {
            SponsorList(sponsors = sponsors)
        }

//        item {
//            LoadingContent(modifier = Modifier ,loader = (vm.sponsorService::getAllSponsor) ) { sponsorList ->
//                sponsorList.let{
//                    Column {
//                        SponsorList(it)
//                        Text(modifier = Modifier.fillMaxWidth(),text = stringResource(id = R.string.sponsor_tip), textAlign = TextAlign.Center, fontSize = 12.sp, color = LocalContentColor.current.copy(0.5f))
//                    }
//                }
//            }
//        }
        item{
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            Text(text = stringResource(id = R.string.join_sponsor), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
        }
        item{
            Spacer(modifier = Modifier.height(12.dp))
        }
        item {
            Row(modifier = Modifier
                .height(80.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colors.surface),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ){
                SponsorIcon(
                    load_url = "https://afdian.net/@funnysaltyfish?tab=home",
                    resourceId = R.drawable.ic_aifadian,
                    contentDes = "爱发电"
                )
                SponsorIcon(
                    load_url = "https://api.funnysaltyfish.fun/alipay.jpg",
                    resourceId = R.drawable.ic_alipay,
                    contentDes = "支付宝"
                )
                SponsorIcon(
                    load_url = "https://api.funnysaltyfish.fun/wechat.png",
                    resourceId = R.drawable.ic_wechat,
                    contentDes = "微信"
                )
            }
        }
    }
}

@Composable
fun SponsorList(
    sponsors : LazyPagingItems<Sponsor>,
) {
    LazyColumn(
        modifier = Modifier.requiredHeightIn(0.dp, 380.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(sponsors){ sponsor ->
            sponsor?.let {
                SponsorItem(sponsor = it)
            }
        }
        val loadStates = sponsors.loadState
        when {
            loadStates.refresh is LoadState.Loading -> {
                item { DefaultLoading() }
            }
            loadStates.append is LoadState.Loading -> {
                item { DefaultLoading() }
            }
            loadStates.refresh is LoadState.Error -> {
                val e = sponsors.loadState.refresh as LoadState.Error
                Log.e("refresh error: %s", e.toString())
                item {
                    DefaultFailure(modifier = Modifier.fillParentMaxSize()) {
                        sponsors.refresh()
                    }
                }
            }
            loadStates.append is LoadState.Error -> {
                val e = sponsors.loadState.append as LoadState.Error
                Log.e("append error: %s", e.toString())
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        DefaultFailure(retry = {
                            sponsors.retry()
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun SponsorIcon(
    load_url : String,
    resourceId : Int,
    contentDes : String
) {
    val context = LocalContext.current
    IconButton(onClick = {
        val intent = Intent(context, WebViewActivity::class.java)
        intent.putExtra("load_url",load_url)
        context.startActivity(intent)
    }, modifier = Modifier.size(64.dp)) {
        Icon(
            modifier = Modifier.size(48.dp) ,
            painter = painterResource(id = resourceId),
            contentDescription = contentDes,
            tint = Color.Unspecified
        )
    }
}

@Composable
fun SponsorItem(
    sponsor : Sponsor,
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(MaterialTheme.colors.surface)
        .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
        .animateContentSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){
        Column(
            modifier = Modifier.weight(7f)
        ){
            Text(sponsor.name, fontSize = 18.sp, fontWeight = FontWeight.W600)
            sponsor.message?.let {
                Text(
                    it,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W500,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp, top = 2.dp),
                    color = LocalContentColor.current.copy(0.7f)
                )
            }
        }
        Column(
            modifier = Modifier.weight(3f)
        ) {
            val fontSize = when(sponsor.money.moneyText.length){
                in 1..5 -> 28
                else -> 24
            }
            Text(text = sponsor.money.moneyText, Modifier.padding(end = 4.dp), fontWeight = W800, fontSize = fontSize.sp)
        }
    }
}

private val Int.moneyText : String
    get() = String.format("%.2f元", this / 100f)
