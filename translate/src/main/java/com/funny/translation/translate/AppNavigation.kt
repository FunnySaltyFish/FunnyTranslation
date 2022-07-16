package com.funny.translation.translate

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navDeepLink
import com.funny.data_saver.core.LocalDataSaver
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.translate.bean.Consts
import com.funny.translation.translate.ui.main.MainScreen
import com.funny.translation.translate.ui.plugin.PluginScreen
import com.funny.translation.translate.ui.screen.TranslateScreen
import com.funny.translation.translate.ui.settings.AboutScreen
import com.funny.translation.translate.ui.settings.SelectLanguage
import com.funny.translation.translate.ui.settings.SettingsScreen
import com.funny.translation.translate.ui.settings.SortResult
import com.funny.translation.translate.ui.thanks.ThanksScreen
import com.funny.translation.translate.ui.theme.TransTheme
import com.funny.translation.translate.ui.widget.CustomNavigation
import com.funny.translation.translate.ui.widget.MarkdownText
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.navigation
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch

private const val TAG = "AppNav"
val LocalNavController = staticCompositionLocalOf<NavController> {
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
@Composable
fun AppNavigation(
    exitAppAction: () -> Unit
) {
    val navController = rememberAnimatedNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val activityVM: ActivityViewModel = viewModel()

    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    BackHandler(enabled = true) {
        if (navController.previousBackStackEntry == null) {
            val curTime = System.currentTimeMillis()
            if (curTime - activityVM.lastBackTime > 2000) {
                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(
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
        LocalSnackbarState provides scaffoldState.snackbarHostState,
        LocalDataSaver provides DataSaverUtils
    ) {
        TransTheme {
            val systemUiController = rememberSystemUiController()
            val useDarkIcons = MaterialTheme.colors.isLight
            val navigationBarColor = MaterialTheme.colors.background.copy(alpha = 0.95f)
            LaunchedEffect(key1 = systemUiController) {
                systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = useDarkIcons)
                systemUiController.setNavigationBarColor(
                    if (!useDarkIcons) Color.Transparent else navigationBarColor,
                    darkIcons = useDarkIcons
                )

                systemUiController.isNavigationBarVisible =
                    !DataSaverUtils.readData(Consts.KEY_HIDE_NAVIGATION_BAR, false)
                systemUiController.isStatusBarVisible =
                    !DataSaverUtils.readData(Consts.KEY_HIDE_STATUS_BAR, true)
            }
            Scaffold(
                bottomBar = {
                    val currentScreen = navController.currentScreenAsState()
                    CustomNavigation(
                        backgroundColor = navigationBarColor,
                        contentPadding = WindowInsets.navigationBars.asPaddingValues(),
                        screens = arrayOf(
                            TranslateScreen.MainScreen,
                            TranslateScreen.PluginScreen,
                            TranslateScreen.SettingScreen,
                            TranslateScreen.ThanksScreen
                        ),
                        currentScreen = currentScreen.value
                    ) { screen ->
                        if (screen == currentScreen) return@CustomNavigation

                        val currentRoute = navBackStackEntry?.destination?.route
                        Log.d(TAG, "AppNavigation: $currentRoute")

                        //currentScreen = screen
                        navController.navigate(screen.route) {
                            //当底部导航导航到在非首页的页面时，执行手机的返回键 回到首页
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                                //currentScreen = TranslateScreen.MainScreen
                            }
                            //从名字就能看出来 跟activity的启动模式中的SingleTop模式一样 避免在栈顶创建多个实例
                            launchSingleTop = true
                            //切换状态的时候保存页面状态
                            restoreState = true
                        }
                    }
                },
                scaffoldState = scaffoldState
            ) {
                AnimatedNavHost(
                    navController = navController,
                    startDestination = TranslateScreen.MainScreen.route,
                    modifier = Modifier.statusBarsPadding()
                        // avoid content being sheltered
                        .padding(bottom = it.calculateBottomPadding())
                ) {
                    composable(TranslateScreen.MainScreen.route, deepLinks = listOf(
                        navDeepLink {
                            uriPattern =
                                "funny://translation/translate?text={text}&sourceId={sourceId}&targetId={targetId}"
                        }
                    )) {
                        MainScreen()
                    }
                    val animDuration = 700
                    navigation(
                        startDestination = TranslateScreen.SettingScreen.route,
                        route = "nav_composition_setting",
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
                        composable(TranslateScreen.SettingScreen.route) {
                            SettingsScreen()
                        }
                        composable(
                            TranslateScreen.AboutScreen.route,
                        ) {
                            AboutScreen()
                        }
                        composable(
                            TranslateScreen.SortResultScreen.route,
                        ) {
                            SortResult(Modifier.fillMaxSize())
                        }
                        composable(
                            TranslateScreen.SelectLanguageScreen.route
                        ) {
                            SelectLanguage(modifier = Modifier.fillMaxSize())
                        }
                    }

                    composable(TranslateScreen.PluginScreen.route) {
                        PluginScreen()
                    }
                    composable(TranslateScreen.ThanksScreen.route) {
                        ThanksScreen()
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
                        MarkdownText(markdown = "请认真阅读并同意[隐私政策](https://api.funnysaltyfish.fun/trans/v1/api/privacy)后，方可使用本应用")
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

@Stable
@Composable
private fun NavHostController.currentScreenAsState(): MutableState<TranslateScreen> {
    val selectedItem = remember { mutableStateOf<TranslateScreen>(TranslateScreen.MainScreen) }

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