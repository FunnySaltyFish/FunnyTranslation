package com.funny.translation.translate.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W300
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.funny.cmaterialcolors.MaterialColors
import com.funny.compose.loading.loadingList
import com.funny.compose.loading.rememberRetryableLoadingState
import com.funny.jetsetting.core.JetSettingTile
import com.funny.jetsetting.core.ui.FunnyIcon
import com.funny.jetsetting.core.ui.SettingItemCategory
import com.funny.translation.AppConfig
import com.funny.translation.WebViewActivity
import com.funny.translation.helper.openUrl
import com.funny.translation.helper.rememberFastClickHandler
import com.funny.translation.helper.toastOnUi
import com.funny.translation.theme.isLight
import com.funny.translation.translate.BuildConfig
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.LocalNavController
import com.funny.translation.translate.R
import com.funny.translation.translate.RegionConfig
import com.funny.translation.translate.bean.OpenSourceLibraryInfo
import com.funny.translation.translate.ui.screen.TranslateScreen
import com.funny.translation.translate.ui.widget.CommonPage
import com.funny.translation.translate.ui.widget.ShadowedRoundImage
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.touchToScale

@Composable
@Preview
fun AboutScreen() {
    val context = LocalContext.current
    val navController = LocalNavController.current
    CommonPage(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        title = stringResource(id = R.string.about)
    ) {
        LargeImageTile(
            funnyIcon = FunnyIcon(resourceId = R.drawable.ic_launcher_icon),
            text = "译站 | Transtation",
            description = "v${AppConfig.versionName}, ${stringResource(id = R.string.build_type)}${BuildConfig.BUILD_TYPE}"
        )
        SettingItemCategory(title = {
            Text(text = stringResource(id = R.string.developer))
        }) {
            val fastClickHandler = rememberFastClickHandler {
                AppConfig.developerMode.value = true
                context.toastOnUi(R.string.open_developer_mode)
            }
            LargeImageTile(
                modifier = Modifier.clickable(onClick = fastClickHandler),
                text = "FunnySaltyFish",
                description = stringResource(R.string.my_description),
                funnyIcon = FunnyIcon(resourceId = R.drawable.ic_developer_avatar)
            )
        }
        SettingItemCategory(title = {
            Text(text = stringResource(id = R.string.discussion))
        }) {
            JetSettingTile(
                resourceId = R.drawable.ic_qq,
                text = stringResource(R.string.join_qq_group),
                description = stringResource(R.string.join_qq_group_description)
            ) {
                WebViewActivity.start(context, "https://jq.qq.com/?_wv=1027&k=3Bvvfzdu")
            }
            JetSettingTile(
                imageVector = Icons.Default.Email,
                text = stringResource(R.string.contact_developer_via_email),
                description = stringResource(R.string.contact_developer_via_email_description)
            ) {
                context.openUrl("mailto://funnysaltyfish@foxmail")
            }
        }
        SettingItemCategory(title = {
            Text(text = stringResource(id = R.string.source_code))
        }) {
            JetSettingTile(
                resourceId = R.drawable.ic_github,
                text = stringResource(R.string.source_code),
                description = stringResource(R.string.source_code_description)
            ) {
                context.toastOnUi(FunnyApplication.resources.getText(R.string.welcome_star))
                WebViewActivity.start(context, "https://github.com/FunnySaltyFish/FunnyTranslation")
            }
        }
        SettingItemCategory(title = {
            Text(text = stringResource(id = R.string.more_about))
        }) {
            JetSettingTile(
                resourceId = R.drawable.ic_open_source_library,
                text = stringResource(id = R.string.open_source_library)
            ) {
                navController.navigate(TranslateScreen.OpenSourceLibScreen.route)
            }
            JetSettingTile(
                resourceId = R.drawable.ic_privacy,
                text = stringResource(R.string.privacy)
            ) {
                WebViewActivity.start(
                    context,
                    "https://api.funnysaltyfish.fun/trans/v1/api/privacy"
                )
            }
            // 用户协议
            JetSettingTile(
                resourceId = R.drawable.ic_user_agreement,
                text = stringResource(id = R.string.user_agreement)
            ) {
                WebViewActivity.start(
                    context,
                    "https://api.funnysaltyfish.fun/trans/v1/api/user_agreement"
                )
            }
            if (RegionConfig.beianNumber.isNotEmpty()) {
                JetSettingTile(
                    resourceId = R.drawable.ic_beian,
                    text = stringResource(id = R.string.beian),
                    description = RegionConfig.beianNumber
                ) {
                    WebViewActivity.start(
                        context,
                        "https://beian.miit.gov.cn/#/Integrated/index"
                    )
                }
            }
        }
    }
}

@Composable
private fun LargeImageTile(
    modifier: Modifier = Modifier,
    funnyIcon: FunnyIcon,
    text: String,
    description: String,
) {
    ListItem(
        modifier = modifier,
        headlineContent = {
            Text(text = text, fontWeight = W500, modifier = Modifier.padding(bottom = 2.dp))
        },
        leadingContent = {
            ShadowedRoundImage(modifier = Modifier.size(64.dp), funnyIcon = funnyIcon)
        },
        supportingContent = {
            Text(text = description)
        }
    )
}

@Composable
fun OpenSourceLibScreen() {
    val vm : SettingsScreenViewModel = viewModel()
    val (state, retry) = rememberRetryableLoadingState(loader = vm::loadOpenSourceLibInfo)
    CommonPage(title = stringResource(id = R.string.open_source_library)) {
        LazyColumn(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            loadingList(state, retry, key = { it.name }) { info ->
                val color =
                    if (info.author == "FunnySaltyFish" && MaterialTheme.colorScheme.isLight) MaterialColors.Orange200 else MaterialTheme.colorScheme.primaryContainer
                OpenSourceLibItem(
                    modifier = Modifier
                        .touchToScale()
                        .fillMaxWidth()
                        .clip(shape = RoundedCornerShape(12.dp))
                        .background(color)
                        .padding(top = 12.dp, start = 12.dp, end = 12.dp, bottom = 4.dp),
                    info = info
                )
            }
        }
    }
}

@Composable
fun OpenSourceLibItem(
    modifier: Modifier = Modifier,
    info: OpenSourceLibraryInfo
) {
    val context = LocalContext.current
    Column(modifier = modifier){
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = info.name, fontWeight = W700)
            Text(text = info.author, fontWeight = W300, fontSize = 12.sp)
        }
        Row(Modifier.fillMaxSize(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(modifier = Modifier.weight(6f), text = info.description, fontWeight = W500, fontSize = 14.sp)
            IconButton(modifier = Modifier
                .size(48.dp)
                .weight(1f), onClick = {
                WebViewActivity.start(context, info.url)
            }) {
                FixedSizeIcon(Icons.Default.KeyboardArrowRight, contentDescription = stringResource(R.string.browser_url))
            }
        }
    }
}

//@Composable
//@Preview
//fun OpenSourceLibItemPreview() {
//    OpenSourceLibItem(modifier = Modifier
//        .fillMaxWidth()
//        .wrapContentHeight(), info = OpenSourceLibraryInfo(
//        name="ComposeDataSaver",
//        url= "https://github.com/FunnySaltyFish/ComposeDataSaver",
//        description= "在 Jetpack Compose 中优雅完成数据持久化",
//        author= "FunnySaltyFish"
//    ))
//}