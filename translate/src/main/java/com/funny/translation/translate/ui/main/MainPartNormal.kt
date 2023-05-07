package com.funny.translation.translate.ui.main

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ModalDrawer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.translation.AppConfig
import com.funny.translation.translate.*
import com.funny.translation.translate.R
import com.funny.translation.translate.activity.WebViewActivity
import com.funny.translation.translate.ui.screen.TranslateScreen
import com.funny.translation.translate.ui.thanks.UserInfoPanel
import com.funny.translation.translate.ui.widget.ExchangeButton
import com.funny.translation.translate.ui.widget.NoticeBar
import kotlinx.coroutines.launch

// 主页面，在未输入状态下展示的页面，默认


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainPartNormal(
    vm: MainViewModel,
    showEngineSelectAction: () -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerContent = {
            Drawer(
                Modifier
                    .fillMaxHeight()
                    .width(300.dp)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(12.dp)
            )
        },
        drawerState = drawerState
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UpperPartBackground {
                MainTopBarNormal(showDrawerAction = { scope.launch { drawerState.open() } })
                Notice(Modifier.fillMaxWidth(0.9f))
                Spacer(modifier = Modifier.height(8.dp))
                HintText(onClick = { vm.updateMainScreenState(MainScreenState.Inputting) })
            }
            Spacer(modifier = Modifier.height(8.dp))
            LanguageSelectRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                sourceLanguage = vm.sourceLanguage,
                updateSourceLanguage = vm::updateSourceLanguage,
                targetLanguage = vm.targetLanguage,
                updateTargetLanguage = vm::updateTargetLanguage,
            )
            Spacer(modifier = Modifier.height(8.dp))
            FunctionsRow(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 40.dp),
                showEngineSelectAction
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Drawer(
    modifier: Modifier = Modifier
) {
    val navController = LocalNavController.current
    val drawerItemIcon = @Composable { resId: Int, contentDescription: String ->
        Icon(
            painter = painterResource(id = resId),
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
    Column(modifier) {
        UserInfoPanel(navHostController = navController)
        Spacer(modifier = Modifier.height(8.dp))
        NavigationDrawerItem(
            icon = {
                drawerItemIcon(R.drawable.ic_settings, stringResource(id = R.string.nav_settings))
            },
            label = { Text(text = stringResource(id = R.string.nav_settings)) },
            selected = false,
            onClick = {
                navController.navigateSingleTop(TranslateScreen.SettingScreen.route)
            })
        // thanks
        NavigationDrawerItem(
            icon = {
                drawerItemIcon(R.drawable.ic_thanks, stringResource(id = R.string.nav_thanks))
            },
            label = { Text(text = stringResource(id = R.string.nav_thanks)) },
            selected = false,
            onClick = {
                navController.navigateSingleTop(TranslateScreen.ThanksScreen.route)
            })
        Spacer(modifier = Modifier.height(8.dp))
        Divider()
    }
}

@Composable
private fun HintText(
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    var shouldRequestFocus by remember { mutableStateOf(AppConfig.sAutoFocus.value) }
    DisposableEffect(Unit) {
        onDispose { shouldRequestFocus = false }
    }

    Text(
        text = stringResource(id = R.string.trans_text_input_hint),
        fontSize = 28.sp,
        fontWeight = FontWeight.W600,
        color = Color.LightGray,
        textAlign = TextAlign.Start,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    )

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Notice(modifier: Modifier) {
    var singleLine by remember { mutableStateOf(true) }
    val activityVM = LocalActivityVM.current
    val notice by activityVM.noticeInfo
    val context = LocalContext.current
    notice?.let {
        NoticeBar(
            modifier = modifier
                .clickable {
                    if (it.url.isNullOrEmpty()) singleLine = !singleLine
                    else WebViewActivity.start(context, it.url)
                }
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    RoundedCornerShape(8.dp)
                )
                .padding(8.dp)
                .animateContentSize()
                .apply {
                    if (singleLine) this.basicMarquee()
                },
            text = it.message,
            singleLine = singleLine,
            showClose = true,
        )
    }
}

@Composable
fun FunctionsRow(
    modifier: Modifier = Modifier,
    showEngineSelectAction: () -> Unit = {},
) {
    val navHostController = LocalNavController.current

    // 从左至右，图片翻译、选择引擎、收藏夹
    Row(modifier, horizontalArrangement = Arrangement.SpaceAround) {
        FunctionIconItem(
            iconId = R.drawable.ic_album,
            text = stringResource(id = R.string.image_translate)
        ) {
            navHostController.navigateSingleTop(TranslateScreen.ImageTranslateScreen.route)
        }
        FunctionIconItem(
            iconId = R.drawable.ic_translate,
            text = stringResource(id = R.string.engine_select),
            onClick = showEngineSelectAction
        )
        FunctionIconItem(
            iconId = R.drawable.ic_star_filled,
            text = stringResource(id = R.string.favorites)
        ) {
//            navHostController.navigateSingleTop(TranslateScreen.FavoritesScreen.route)
        }
    }
}

@Composable
private fun FunctionIconItem(
    iconId: Int,
    text: String,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape), onClick = onClick, colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = text,
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
internal fun LanguageSelectRow(
    modifier: Modifier,
    exchangeButtonTint: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    sourceLanguage: Language,
    updateSourceLanguage: (Language) -> Unit,
    targetLanguage: Language,
    updateTargetLanguage: (Language) -> Unit,
) {
    val enabledLanguages by enabledLanguages.collectAsState()
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LanguageSelect(
            Modifier.semantics {
                contentDescription = appCtx.getString(R.string.des_current_source_lang)
            },
            language = sourceLanguage,
            languages = enabledLanguages,
            updateLanguage = updateSourceLanguage
        )
        ExchangeButton(tint = exchangeButtonTint) {
            val temp = sourceLanguage
            updateSourceLanguage(targetLanguage)
            updateTargetLanguage(temp)
        }
        LanguageSelect(
            Modifier.semantics {
                contentDescription = appCtx.getString(R.string.des_current_target_lang)
            },
            language = targetLanguage,
            languages = enabledLanguages,
            updateLanguage = updateTargetLanguage
        )
    }
}

@Composable
fun MainTopBarNormal(
    showDrawerAction: (() -> Unit)?,
) {
    val navController = LocalNavController.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        if (showDrawerAction != null) {
            IconButton(onClick = showDrawerAction) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = stringResource(id = R.string.menu)
                )
            }
        }
        IconButton(
            onClick = {
                navController.navigateSingleTop(TranslateScreen.PluginScreen.route)
            }, modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.End)
        ) {
            Icon(
                painterResource(id = R.drawable.ic_plugin),
                contentDescription = stringResource(id = R.string.manage_plugins)
            )
        }
    }
}


