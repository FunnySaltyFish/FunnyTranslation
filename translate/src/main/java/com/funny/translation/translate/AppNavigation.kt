package com.funny.translation.translate

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.funny.data_saver.core.LocalDataSaver
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.translation.AppConfig
import com.funny.translation.Consts
import com.funny.translation.NeedToTransConfig
import com.funny.translation.bean.TranslationConfig
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.Log
import com.funny.translation.helper.NAV_ANIM_DURATION
import com.funny.translation.helper.animateComposable
import com.funny.translation.theme.TransTheme
import com.funny.translation.translate.bean.AI_TEXT_POINT
import com.funny.translation.translate.ui.TranslateScreen
import com.funny.translation.translate.ui.ai.ChatScreen
import com.funny.translation.translate.ui.buy.BuyAIPointScreen
import com.funny.translation.translate.ui.buy.TransProScreen
import com.funny.translation.translate.ui.long_text.DraftScreen
import com.funny.translation.translate.ui.long_text.LongTextTransDetailScreen
import com.funny.translation.translate.ui.long_text.LongTextTransListScreen
import com.funny.translation.translate.ui.long_text.LongTextTransScreen
import com.funny.translation.translate.ui.long_text.TextEditorAction
import com.funny.translation.translate.ui.long_text.TextEditorScreen
import com.funny.translation.translate.ui.main.FavoriteScreen
import com.funny.translation.translate.ui.main.ImageTransScreen
import com.funny.translation.translate.ui.main.MainScreen
import com.funny.translation.translate.ui.plugin.PluginScreen
import com.funny.translation.translate.ui.settings.*
import com.funny.translation.translate.ui.thanks.AnnualReportScreen
import com.funny.translation.translate.ui.thanks.AppRecommendationScreen
import com.funny.translation.translate.ui.thanks.ThanksScreen
import com.funny.translation.translate.ui.thanks.addUserProfileRoutes
import com.funny.translation.translate.utils.DeepLinkManager
import com.funny.translation.ui.MarkdownText
import kotlinx.coroutines.launch
import java.util.UUID

private const val TAG = "AppNav"
val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("NavController has not been initialized! ")
}
val LocalSnackbarState = staticCompositionLocalOf<SnackbarHostState> {
    error("LocalSnackbarState has not been initialized! ")
}
val LocalActivityVM = staticCompositionLocalOf<ActivityViewModel> {
    error("Local ActivityVM has not been initialized! ")
}

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppNavigation(
    navController: NavHostController,
    exitAppAction: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(key1 = navController) {
        (context as TransActivity).navController = navController
    }

    val activityVM: ActivityViewModel = viewModel()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember {
        SnackbarHostState()
    }

    BackHandler(enabled = true) {
        if (navController.previousBackStackEntry == null) {
            val curTime = System.currentTimeMillis()
            if (curTime - activityVM.lastBackTime > 2000) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        FunnyApplication.resources.getString(
                            R.string.snack_quit
                        )
                    )
                }
                activityVM.lastBackTime = curTime
            } else {
                exitAppAction()
            }
        } else {
            Log.d(TAG, "AppNavigation: back")
            //currentScreen = TranslateScreen.MainScreen
        }
    }

    CompositionLocalProvider(
        LocalNavController provides navController,
        LocalSnackbarState provides snackbarHostState,
        LocalDataSaver provides DataSaverUtils
    ) {
        TransTheme {
            Scaffold(
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState)
                }
            ) { scaffoldPadding ->
                NavHost(
                    navController = navController,
                    startDestination = TranslateScreen.MainScreen.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(
                        TranslateScreen.MainScreen.route,
                    ) {
                        MainScreen()
                    }
                    animateComposable(
                        TranslateScreen.ImageTranslateScreen.route,
                        deepLinks = listOf(
                            navDeepLink {
                                uriPattern =
                                    "${DeepLinkManager.PREFIX}${DeepLinkManager.IMAGE_TRANS_PATH}?imageUri={imageUri}&sourceId={sourceId}&targetId={targetId}&doClip={doClip}"
                            }
                        ),
                        arguments = listOf(
                            navArgument("imageUri") {
                                type = NavType.StringType; defaultValue = null; nullable = true
                            },
                            navArgument("sourceId") {
                                type = NavType.IntType; defaultValue = Language.AUTO.id
                            },
                            navArgument("targetId") {
                                type = NavType.IntType; defaultValue = Language.CHINESE.id
                            },
                            navArgument("doClip") {
                                type = NavType.BoolType; defaultValue = false
                            }
                        )
                    ) {
                        // 使用 Intent 跳转目前会导致 Activity 重建
                        // 不合理，相当不合理
                        ImageTransScreen(
                            imageUri = it.arguments?.getString("imageUri")?.toUri(),
                            sourceId = it.arguments?.getInt("sourceId"),
                            targetId = it.arguments?.getInt("targetId"),
                            doClipFirst = it.arguments?.getBoolean("doClip") ?: false
                        )
                    }
                    animateComposable(TranslateScreen.AboutScreen.route) {
                        AboutScreen()
                    }
                    animateComposable(TranslateScreen.PluginScreen.route) {
                        PluginScreen()
                    }
                    animateComposable(TranslateScreen.TransProScreen.route) {
                        TransProScreen()
                    }
                    animateComposable(TranslateScreen.ThanksScreen.route) {
                        ThanksScreen()
                    }
                    animateComposable(TranslateScreen.FloatWindowScreen.route) {
                        FloatWindowScreen()
                    }
                    animateComposable(TranslateScreen.FavoriteScreen.route) {
                        FavoriteScreen()
                    }
                    animateComposable(TranslateScreen.AppRecommendationScreen.route) {
                        AppRecommendationScreen()
                    }
                    val animDuration = NAV_ANIM_DURATION
                    composable(
                        TranslateScreen.ChatScreen.route,
                        enterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Up,
                                animationSpec = tween(animDuration)
                            )
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Up,
                                animationSpec = tween(animDuration)
                            )
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Down,
                                animationSpec = tween(animDuration)
                            )
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Down,
                                animationSpec = tween(animDuration)
                            )
                        }
                    ) {
                        ChatScreen()
                    }
                    animateComposable(
                        TranslateScreen.BuyAIPointScreen.route,
                        arguments = listOf(
                            navArgument("planName") {
                                type = NavType.StringType; defaultValue = null; nullable = true
                            }
                        )
                    ) {
                        val planName = it.arguments?.getString("planName")
                        BuyAIPointScreen(planName ?: AI_TEXT_POINT)
                    }
                    animateComposable(TranslateScreen.AnnualReportScreen.route) {
                        AnnualReportScreen()
                    }
                    addLongTextTransNavigation()
                    addSettingsNavigation()
                    addUserProfileRoutes(
                        navHostController = navController
                    ) { userBean ->
                        Log.d(TAG, "登录成功: 用户: $userBean")
                        if (userBean.isValid()) AppConfig.login(userBean, updateVipFeatures = true)
                    }
                }
            }

            var firstOpenApplication by rememberDataSaverState(
                key = Consts.KEY_FIRST_OPEN_APP,
                default = true
            )
            if (firstOpenApplication) {
                AlertDialog(
                    onDismissRequest = { },
                    text = {
                        MarkdownText(markdown = "我们更新了新的[隐私政策](https://api.funnysaltyfish.fun/trans/v1/api/privacy)和[用户协议](https://api.funnysaltyfish.fun/trans/v1/api/user_agreement)，请认真阅读并同意后，方可使用本应用")
                    },
                    confirmButton = {
                        Button(onClick = { firstOpenApplication = false }) {
                            Text(stringResource(R.string.agree))
                        }
                    },
                    dismissButton = {
                        Button(onClick = exitAppAction) {
                            Text(stringResource(R.string.not_agree))
                        }
                    }
                )
            }
        }
    }

}


private fun NavGraphBuilder.addLongTextTransNavigation() {
    navigation(
        startDestination = TranslateScreen.LongTextTransScreen.route,
        route = "nav_1_long_text_trans"
    ) {
        animateComposable(TranslateScreen.LongTextTransScreen.route) {
            LongTextTransScreen()
        }
        animateComposable(TranslateScreen.LongTextTransListScreen.route) {
            LongTextTransListScreen()
        }
        animateComposable(
            TranslateScreen.LongTextTransDetailScreen.route,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType; defaultValue = null; nullable = true
                }
            )
        ) {
            val id = it.arguments?.getString("id")
            LongTextTransDetailScreen(id = id ?: UUID.randomUUID().toString())
        }
        animateComposable(
            TranslateScreen.TextEditorScreen.route,
            arguments = listOf(
                navArgument("action") {
                    type = NavType.StringType
                }
            )
        ) {
            val action = kotlin.runCatching {
                TextEditorAction.fromString(it.arguments?.getString("action") ?: "")
            }.getOrNull()
            TextEditorScreen(action)
        }
        animateComposable(
            TranslateScreen.DraftScreen.route
        ) {
            DraftScreen()
        }
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addSettingsNavigation() {
    navigation(
        startDestination = TranslateScreen.SettingScreen.route,
        route = "nav_1_setting",
    ) {
        animateComposable(TranslateScreen.SettingScreen.route) {
            SettingsScreen()
        }
        animateComposable(
            TranslateScreen.OpenSourceLibScreen.route,
        ) {
            OpenSourceLibScreen()
        }
        animateComposable(
            TranslateScreen.ThemeScreen.route,
        ) {
            ThemeScreen()
        }
        animateComposable(
            TranslateScreen.SortResultScreen.route,
        ) {
            SortResult(Modifier.fillMaxSize())
        }
        animateComposable(
            TranslateScreen.SelectLanguageScreen.route
        ) {
            SelectLanguage(modifier = Modifier.fillMaxSize())
        }
    }
}

fun NavHostController.navigateSingleTop(route: String, popUpToMain: Boolean = false) {
    navigateSingleTop(uri = NavDestination.createRoute(route).toUri(), popUpToMain = popUpToMain)
}

fun NavHostController.navigateSingleTop(uri: Uri, popUpToMain: Boolean = false) {
    val navController = this
    navController.navigate(uri, navOptions {
        // 先清空其他栈，使得返回时能直接回到主界面
        if (popUpToMain) {
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
                inclusive = false
                //currentScreen = TranslateScreen.MainScreen
            }
        }
        //从名字就能看出来 跟activity的启动模式中的SingleTop模式一样 避免在栈顶创建多个实例
        launchSingleTop = true
        //切换状态的时候保存页面状态
        restoreState = true
    })
}

// 跳转到翻译页面，并开始翻译
fun NavHostController.navigateToTextTrans(
    sourceText: String?,
    sourceLanguage: Language,
    targetLanguage: Language
) {
    if (sourceText?.isNotBlank() == true) {
        NeedToTransConfig = TranslationConfig(sourceText, sourceLanguage, targetLanguage)
    }
    this.navigate(
        route = TranslateScreen.MainScreen.route,
        navOptions {
            launchSingleTop = true
            popUpTo(TranslateScreen.MainScreen.route) {
                inclusive = false
            }
        })
}

// 下面这个方法是配合底部导航栏使用的，但是新版去除了底部导航栏
// 请将代码切到v2.6.1以查看它的效果
@Stable
@Composable
private fun NavHostController.currentScreenAsState(): MutableState<TranslateScreen> {
    val selectedItem: MutableState<TranslateScreen> =
        rememberDataSaverState(Consts.KEY_APP_CURRENT_SCREEN, TranslateScreen.MainScreen)

    DisposableEffect(this) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            when {
                destination.hierarchy.any { it.route == TranslateScreen.MainScreen.route } -> {
                    selectedItem.value = TranslateScreen.MainScreen
                }

                destination.hierarchy.any { it.route == TranslateScreen.SettingScreen.route } -> {
                    selectedItem.value = TranslateScreen.SettingScreen
                }

                destination.hierarchy.any { it.route == TranslateScreen.PluginScreen.route } -> {
                    selectedItem.value = TranslateScreen.PluginScreen
                }

                destination.hierarchy.any { it.route == TranslateScreen.ThanksScreen.route } -> {
                    selectedItem.value = TranslateScreen.ThanksScreen
                }
            }
        }
        addOnDestinationChangedListener(listener)

        onDispose {
            removeOnDestinationChangedListener(listener)
        }
    }

    return selectedItem
}