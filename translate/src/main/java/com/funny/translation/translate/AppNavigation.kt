package com.funny.translation.translate

import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibilityScope
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
import com.funny.data_saver.core.LocalDataSaver
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.translation.AppConfig
import com.funny.translation.Consts
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.theme.TransTheme
import com.funny.translation.translate.ui.main.FavoriteScreen
import com.funny.translation.translate.ui.main.ImageTransScreen
import com.funny.translation.translate.ui.main.MainScreen
import com.funny.translation.translate.ui.plugin.PluginScreen
import com.funny.translation.translate.ui.screen.TranslateScreen
import com.funny.translation.translate.ui.settings.*
import com.funny.translation.translate.ui.thanks.AppRecommendationScreen
import com.funny.translation.translate.ui.thanks.ThanksScreen
import com.funny.translation.translate.ui.thanks.TransProScreen
import com.funny.translation.translate.ui.thanks.addUserProfileRoutes
import com.funny.translation.ui.MarkdownText
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.navigation
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import kotlinx.coroutines.launch

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

@OptIn(ExperimentalLayoutApi::class)
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun AppNavigation(
    exitAppAction: () -> Unit
) {
    val navController = rememberAnimatedNavController()
    val context = LocalContext.current

    LaunchedEffect(key1 = navController){
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
                AnimatedNavHost(
                    navController = navController,
                    startDestination = TranslateScreen.MainScreen.route,
                    modifier = Modifier
                        // 下面的三个看起来很奇怪，但它来自于 https://issuetracker.google.com/issues/249727298
                        // 否则，imePadding() 工作不正常（在三大金刚键的导航模式下会会多出一段）
                        .padding(scaffoldPadding)
                        .consumeWindowInsets(scaffoldPadding)
                        .systemBarsPadding()
                ) {
                    composable(
                        TranslateScreen.MainScreen.route,
                        deepLinks = listOf(
                            navDeepLink {
                                uriPattern =
                                    "funny://translation/translate?text={text}&sourceId={sourceId}&targetId={targetId}"
                            }
                        ),
                        arguments = listOf(
                            navArgument("text") {  },
                            navArgument("sourceId") { type = NavType.IntType; defaultValue = Language.AUTO.id },
                            navArgument("targetId") { type = NavType.IntType; defaultValue = Language.CHINESE.id  }
                        )
                    ) {
                        MainScreen(
                            sourceText = it.arguments?.getString("text"),
                            sourceId = it.arguments?.getInt("sourceId"),
                            targetId = it.arguments?.getInt("targetId")
                        )
                    }
                    animateComposable(
                        TranslateScreen.ImageTranslateScreen.route,
                        deepLinks = listOf(
                            navDeepLink {
                                uriPattern =
                                    "funny://translation/image_translate?imageUri={imageUri}&sourceId={sourceId}&targetId={targetId}&doClip={doClip}"
                            }
                        ),
                        arguments = listOf(
                            navArgument("imageUri") { type = NavType.StringType; defaultValue = null; nullable = true },
                            navArgument("sourceId") { type = NavType.IntType; defaultValue = Language.AUTO.id },
                            navArgument("targetId") { type = NavType.IntType; defaultValue = Language.CHINESE.id  },
                            navArgument("doClip") { type = NavType.BoolType; defaultValue = false  }
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
                    animateComposable(TranslateScreen.ThanksScreen.route){
                        ThanksScreen(navController)
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

fun NavHostController.navigateSingleTop(route: String, popUpToMain: Boolean = false){
    val navController = this
    navController.navigate(route) {
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
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.animateComposable(
    route: String,
    animDuration: Int = 700,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentScope.SlideDirection.Left,
                animationSpec = tween(animDuration)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentScope.SlideDirection.Left,
                animationSpec = tween(animDuration)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentScope.SlideDirection.Right,
                animationSpec = tween(animDuration)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentScope.SlideDirection.Right,
                animationSpec = tween(animDuration)
            )
        }
    ) {
        content(it)
    }
}

// 跳转到翻译页面，并开始翻译
fun NavHostController.navigateToTextTrans(sourceText: String?, sourceLanguage: Language, targetLanguage: Language) {
    val text = Uri.encode(sourceText)
    this.navigate(
        NavDeepLinkRequest.Builder
            .fromUri(Uri.parse("funny://translation/translate?text=$text&sourceId=${sourceLanguage.id}&targetId=${targetLanguage.id}"))
            .build(),
        navOptions = NavOptions.Builder()
            .setPopUpTo(TranslateScreen.MainScreen.route, true)
            .setLaunchSingleTop(true)
//             .setRestoreState(true)
            .build()
    )
}

// 下面这个方法是配合底部导航栏使用的，但是新版去除了底部导航栏
// 请将代码切到v2.6.1以查看它的效果
@Stable
@Composable
private fun NavHostController.currentScreenAsState(): MutableState<TranslateScreen> {
    val selectedItem: MutableState<TranslateScreen> = rememberDataSaverState(Consts.KEY_APP_CURRENT_SCREEN, TranslateScreen.MainScreen)

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