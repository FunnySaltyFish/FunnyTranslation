package com.funny.translation.translate.ui.main

import android.content.Intent
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.funny.compose.loading.LoadingContent
import com.funny.trans.login.LoginActivity
import com.funny.translation.AppConfig
import com.funny.translation.WebViewActivity
import com.funny.translation.helper.SimpleAction
import com.funny.translation.translate.*
import com.funny.translation.translate.R
import com.funny.translation.translate.ui.screen.TranslateScreen
import com.funny.translation.translate.ui.widget.*
import com.funny.translation.ui.touchToScale
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// 主页面，在未输入状态下展示的页面，默认
@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun MainPartNormal(
    vm: MainViewModel,
    isScreenHorizontal: Boolean,
    showEngineSelectAction: () -> Unit,
    openDrawerAction: SimpleAction?,
) {
    val swipeableState = rememberSwipeableState(SwipeShowType.Main)
    val scope = rememberCoroutineScope()

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // 因为 HistoryScreen 是列表，如果滑到底部仍然有多余的滑动距离，就关闭
                // Log.d("NestedScrollConnection", "onPostScroll: $available")
                if (!swipeableState.isAnimationRunning && source == NestedScrollSource.Drag && available.y < -30.0f) {
                    scope.launch {
                        swipeableState.animateTo(SwipeShowType.Main)
                    }
                    return Offset(0f, available.y)
                }
                return super.onPostScroll(consumed, available, source)
            }
        }
    }

    val activityVM = LocalActivityVM.current
    LaunchedEffect(key1 = activityVM) {
        activityVM.activityLifecycleEvent.collect {
            Log.d("MainPartNormal", "activityLifecycleEvent: $it")
            when (it) {
                Lifecycle.Event.ON_RESUME -> {
                    if (AppConfig.sAutoFocus.value && swipeableState.currentValue == SwipeShowType.Main) {
                        vm.updateMainScreenState(MainScreenState.Inputting)
                    }
                }

                else -> Unit
            }
        }
    }

    // 返回键关闭
    BackHandler(swipeableState.currentValue == SwipeShowType.Foreground) {
        scope.launch {
            swipeableState.animateTo(SwipeShowType.Main)
        }
    }

    val progressState = remember { mutableStateOf(1f) }
    SwipeCrossFadeLayout(
        state = swipeableState,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        onProgressChanged = { progressState.value = it },

        mainUpper = {
            UpperPartBackground {
                MainTopBarNormal(showDrawerAction = openDrawerAction)
                Notice(Modifier.fillMaxWidth(0.9f))
                Spacer(modifier = Modifier.height(8.dp))
                HintText(onClick = { vm.updateMainScreenState(MainScreenState.Inputting) })
            }
        },
        mainLower = {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                LanguageSelectRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp, vertical = 12.dp),
                    sourceLanguage = vm.sourceLanguage,
                    updateSourceLanguage = vm::updateSourceLanguage,
                    targetLanguage = vm.targetLanguage,
                    updateTargetLanguage = vm::updateTargetLanguage,
                )
                Spacer(modifier = Modifier.height(8.dp))
                FunctionsRow(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 24.dp,
                            end = 24.dp,
                            top = 8.dp,
                            bottom = if (isScreenHorizontal) 8.dp else 40.dp
                        ),
                    showEngineSelectAction
                )
            }
        },
        foreground = {
            HistoryScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection),
                progressProvider = { progressState.value }
            ) {
                scope.launch {
                    swipeableState.animateTo(SwipeShowType.Main)
                }
            }
        }
    )
}

@Composable
private fun HintText(
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
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
            navHostController.navigateSingleTop(TranslateScreen.FavoriteScreen.route)
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
    ChildrenFixedSizeRow(
        modifier = modifier,
        elementsPadding = 16.dp,
        left = {
            LanguageSelect(
                Modifier.semantics {
                    contentDescription = appCtx.getString(R.string.des_current_source_lang)
                },
                language = sourceLanguage,
                languages = enabledLanguages,
                updateLanguage = updateSourceLanguage
            )
        }, center = {
            ExchangeButton(tint = exchangeButtonTint) {
                val temp = sourceLanguage
                updateSourceLanguage(targetLanguage)
                updateTargetLanguage(temp)
            }
        }, right = {
            LanguageSelect(
                Modifier.semantics {
                    contentDescription = appCtx.getString(R.string.des_current_target_lang)
                },
                language = targetLanguage,
                languages = enabledLanguages,
                updateLanguage = updateTargetLanguage
            )
        }
    )
}

@Composable
private fun ChildrenFixedSizeRow(
    modifier: Modifier = Modifier,
    elementsPadding: Dp = 40.dp,
    left: @Composable () -> Unit,
    center: @Composable () -> Unit,
    right: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val ep = remember(elementsPadding) {
        density.run {
            elementsPadding.toPx().roundToInt()
        }
    }
    SubcomposeLayout(modifier) { constraints: Constraints ->
        val allWidth = constraints.maxWidth
        val centerPlaceable = subcompose("center", center).first().measure(
            constraints.copy(minWidth = 0)
        )
        val centerWidth = centerPlaceable.width
        val w = ((allWidth - centerWidth - 2 * ep) / 2)
        val leftPlaceable = subcompose("left", left).first().measure(
            constraints.copy(minWidth = w, maxWidth = w)
        )
        val rightPlaceable = subcompose("right", right).first().measure(
            constraints.copy(minWidth = w, maxWidth = w)
        )
        val h = maxOf(centerPlaceable.height, leftPlaceable.height, rightPlaceable.height)
        layout(constraints.maxWidth, h) {
            leftPlaceable.placeRelative(0, (h - leftPlaceable.height) / 2)
            centerPlaceable.placeRelative(w + ep, (h - centerPlaceable.height) / 2)
            rightPlaceable.placeRelative(allWidth - w, (h - rightPlaceable.height) / 2)
        }
    }
}

@Composable
private fun MainTopBarNormal(
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

@Composable
private fun LanguageSelect(
    modifier: Modifier = Modifier,
    language: Language,
    languages: List<Language>,
    updateLanguage: (Language) -> Unit,
) {
    var expanded by remember {
        mutableStateOf(false)
    }
    Button(
        modifier = modifier, onClick = {
            expanded = true
        }, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ), contentPadding = PaddingValues(horizontal = 0.dp, vertical = 16.dp)
    ) {
        AutoResizedText(
            text = language.displayText,
            style = LocalTextStyle.current.copy(fontSize = 18.sp, fontWeight = FontWeight.W600),
            byHeight = false
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languages.forEach {
                DropdownMenuItem(onClick = {
                    updateLanguage(it)
                    expanded = false
                }, text = {
                    Text(it.displayText)
                })
            }
        }
    }
}

@Composable
fun UserInfoPanel(navHostController: NavHostController) {
    val TAG = "UserInfoPanel"
    val activityVM = LocalActivityVM.current
    val context = LocalContext.current

    LaunchedEffect(key1 = activityVM.uid) {
        Log.d(TAG, "UserInfoPanel: uid is: ${activityVM.uid}, token is: ${activityVM.token}")
    }

    val startLoginLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        Log.d(TAG, "UserInfoPanel: resultData: ${it.data}")
    }

    LoadingContent(
        retryKey = activityVM.uid,
        updateRetryKey = { startLoginLauncher.launch(Intent(context, LoginActivity::class.java)) },
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
                Box {
                    AsyncImage(
                        model = userBean.avatar_url,
                        contentDescription = "头像",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        placeholder = painterResource(R.drawable.ic_loading)
                    )
                    if (userBean.isValidVip()) {
                        Icon(

                            modifier = Modifier
                                .size(32.dp)
                                .offset(70.dp, 70.dp),
                            painter = painterResource(id = R.drawable.ic_vip),
                            contentDescription = "VIP",
                            tint = Color.Unspecified
                        )
                    }
                }
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
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}


