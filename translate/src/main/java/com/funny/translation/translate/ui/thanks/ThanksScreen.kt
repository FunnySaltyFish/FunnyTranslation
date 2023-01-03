package com.funny.translation.translate.ui.thanks

import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.ExtraBold
import androidx.compose.ui.text.font.FontWeight.Companion.W800
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.compose.AsyncImage
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.trans.login.LoginActivity
import com.funny.translation.translate.LocalActivityVM
import com.funny.translation.translate.R
import com.funny.translation.translate.activity.WebViewActivity
import com.funny.translation.translate.navigateSingleTop
import com.funny.translation.translate.ui.screen.TranslateScreen
import com.funny.translation.translate.ui.widget.DefaultFailure
import com.funny.translation.translate.ui.widget.DefaultLoading
import com.funny.translation.translate.ui.widget.HeadingText
import com.funny.translation.translate.ui.widget.LoadingContent
import com.funny.translation.ui.touchToScale

enum class SponsorSortType(val value: String){
    Date("date"), Money("money")
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ThanksScreen(navHostController: NavHostController) {
    val vm: ThanksViewModel = viewModel()
    val sponsors = vm.sponsors.collectAsLazyPagingItems()
    var sponsorSortType : SponsorSortType by rememberDataSaverState("KEY_SPONSOR_SORT_TYPE", SponsorSortType.Money)
    var sponsorSortOrder by rememberDataSaverState(key = "KEY_SPONSOR_SORT_ORDER", default = -1) // 1 升序; -1 降序

    LaunchedEffect(sponsorSortType, sponsorSortOrder){
        vm.updateSort(sponsorSortType, sponsorSortOrder)
        sponsors.refresh()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            UserInfoPanel(navHostController)
        }
        item {
            HeadingText(text = stringResource(id = R.string.join_sponsor))
        }
        item {
            Row(
                modifier = Modifier
                    .touchToScale()
                    .height(80.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
        stickyHeader {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HeadingText(text = stringResource(id = R.string.thanks))
                SortSponsor(sortType = sponsorSortType, updateSortType = { sponsorSortType = it }, sortOrder = sponsorSortOrder, updateSortOrder = { sponsorSortOrder = it})
            }

        }
        items(sponsors, key = { it.key }) { sponsor ->
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

        item {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.sponsor_tip),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = LocalContentColor.current.copy(0.5f),
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun SortSponsor(
    sortType: SponsorSortType,
    updateSortType: (SponsorSortType) -> Unit,
    sortOrder: Int,
    updateSortOrder: (Int) -> Unit
) {
    // Menu
    var expanded by remember { mutableStateOf(false) }

    IconButton(onClick = {
        expanded = true
    }) {
        Icon(Icons.Default.Sort, contentDescription = stringResource(id = R.string.sort))
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
            modifier = Modifier
                .width(200.dp)
                .height(200.dp)
        ) {
            DropdownMenuItem(onClick = {
                updateSortType(SponsorSortType.Date)
                updateSortOrder(1)
                expanded = false
            } ,text = {
                Text(text = stringResource(id = R.string.sort_by_date_asc))
            }, trailingIcon = {
                if(sortType == SponsorSortType.Date && sortOrder == 1){
                    Icon(Icons.Default.Check, contentDescription = stringResource(id = R.string.sort_by_date_asc))
                }
            })
            DropdownMenuItem(onClick = {
                updateSortType(SponsorSortType.Date)
                updateSortOrder(-1)
                expanded = false
            } ,text = {
                Text(text = stringResource(id = R.string.sort_by_date_desc))
            }, trailingIcon = {
                if(sortType == SponsorSortType.Date && sortOrder == -1){
                    Icon(Icons.Default.Check, contentDescription = stringResource(id = R.string.sort_by_date_desc))
                }
            })
            DropdownMenuItem(onClick = {
                updateSortType(SponsorSortType.Money)
                updateSortOrder(1)
                expanded = false
            } ,text = {
                Text(text = stringResource(id = R.string.sort_by_money_asc))
            }, trailingIcon = {
                if(sortType == SponsorSortType.Money && sortOrder == 1){
                    Icon(Icons.Default.Check, contentDescription = stringResource(id = R.string.sort_by_money_asc))
                }
            })
            DropdownMenuItem(onClick = {
                updateSortType(SponsorSortType.Money)
                updateSortOrder(-1)
                expanded = false
            } ,text = {
                Text(text = stringResource(id = R.string.sort_by_money_desc))
            }, trailingIcon = {
                if(sortType == SponsorSortType.Money && sortOrder == -1){
                    Icon(Icons.Default.Check, contentDescription = stringResource(id = R.string.sort_by_money_desc))
                }
            })
        }
    }
}

@Composable
fun UserInfoPanel(navHostController: NavHostController) {
    val TAG = "UserInfoPanel"
    val activityVM = LocalActivityVM.current
    val context = LocalContext.current

    LaunchedEffect(key1 = activityVM.uid){
        Log.d(TAG, "UserInfoPanel: uid is: ${activityVM.uid}, token is: ${activityVM.token}")
    }

    val startLoginLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        Log.d(TAG, "UserInfoPanel: resultData: ${it.data}")
    }

    LoadingContent(
        key = activityVM.uid,
        updateKey = { startLoginLauncher.launch(Intent(context, LoginActivity::class.java)) },
        modifier = Modifier
            .touchToScale {
                if (activityVM.uid <= 0) { // 未登录
                    startLoginLauncher.launch(Intent(context, LoginActivity::class.java))
                } else {
                    navHostController.navigateSingleTop(
                        TranslateScreen.UserProfileScreen.route,
                        false
                    )
                }
            }
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
            .padding(vertical = 12.dp),
        loader = { activityVM.userInfo }
    ) { userBean ->
        if (userBean.isValid()) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                    model = userBean.avatar_url, contentDescription = "头像", modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    placeholder = painterResource(R.drawable.ic_loading)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${userBean.username} | uid: ${userBean.uid}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400,
                    color = LocalContentColor.current.copy(0.8f)
                )
            }
        } else {
            Text(
                text = stringResource(R.string.login_or_register),
                fontSize = 24.sp,
                fontWeight = ExtraBold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

}

@Composable
fun SponsorIcon(
    load_url: String,
    resourceId: Int,
    contentDes: String
) {
    val context = LocalContext.current
    IconButton(onClick = {
        val intent = Intent(context, WebViewActivity::class.java)
        intent.putExtra("load_url", load_url)
        context.startActivity(intent)
    }, modifier = Modifier.size(64.dp)) {
        Icon(
            modifier = Modifier.size(48.dp),
            painter = painterResource(id = resourceId),
            contentDescription = contentDes,
            tint = Color.Unspecified
        )
    }
}

@Composable
fun SponsorItem(
    sponsor: Sponsor,
) {
    Row(
        modifier = Modifier
            .touchToScale()
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(sponsor.name, fontSize = 18.sp, fontWeight = FontWeight.W600)
            sponsor.message?.let {
                Text(
                    it,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W500,
                    modifier = Modifier.padding(bottom = 8.dp, top = 2.dp, end = 4.dp),
                    color = LocalContentColor.current.copy(0.7f)
                )
            }
        }

        val fontSize = rememberSaveable {
            when (sponsor.money.moneyText.length) {
                in 1..5 -> 28
                else -> 24
            }
        }

        Text(
            text = sponsor.money.moneyText,
            Modifier.padding(end = 4.dp),
            fontWeight = W800,
            fontSize = fontSize.sp
        )
    }
}


private val Int.moneyText: String
    get() = String.format("%.2f元", this / 100f)
