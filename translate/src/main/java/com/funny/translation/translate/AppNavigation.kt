package com.funny.translation.translate

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navDeepLink
import com.funny.data_saver.core.LocalDataSaver
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.translate.bean.Consts
import com.funny.translation.translate.ui.main.MainScreen
import com.funny.translation.translate.ui.plugin.PluginScreen
import com.funny.translation.translate.ui.screen.TranslateScreen
import com.funny.translation.translate.ui.settings.AboutScreen
import com.funny.translation.translate.ui.settings.SettingsScreen
import com.funny.translation.translate.ui.thanks.ThanksScreen
import com.funny.translation.translate.ui.theme.TransTheme
import com.funny.translation.translate.ui.widget.BottomNavigationHeight
import com.funny.translation.translate.ui.widget.CustomNavigation
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.statusBarsPadding
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
        ProvideWindowInsets {
            val bottomBarContentPadding = rememberInsetsPaddingValues(
                LocalWindowInsets.current.navigationBars
            )
            TransTheme {
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = MaterialTheme.colors.isLight
                val navigationBarColor = MaterialTheme.colors.background.copy(alpha = 0.95f)
                LaunchedEffect(key1 = null) {
                    systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = useDarkIcons)
                    systemUiController.setNavigationBarColor(
                        if (!useDarkIcons) Color.Transparent else navigationBarColor, darkIcons = useDarkIcons
                    )

                    systemUiController.isNavigationBarVisible = !DataSaverUtils.readData(Consts.KEY_HIDE_NAVIGATION_BAR,false)
                    systemUiController.isStatusBarVisible = !DataSaverUtils.readData(Consts.KEY_HIDE_STATUS_BAR,true)
                }
                Scaffold(
                    bottomBar = {
                        val currentScreen = navController.currentScreenAsState()
                        CustomNavigation(
                            backgroundColor = navigationBarColor,
                            contentPadding = bottomBarContentPadding,
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
                        modifier = Modifier
                            .statusBarsPadding()
                            // avoid content being sheltered
                            .padding(bottom = BottomNavigationHeight + bottomBarContentPadding.calculateBottomPadding())
                    ) {
                        composable(TranslateScreen.MainScreen.route, deepLinks = listOf(
                            navDeepLink { uriPattern = "funny://translation/translate?text={text}&sourceId={sourceId}&targetId={targetId}" }
                        )) { navBackStackEntry ->
                            MainScreen(
                                translateText = activityVM.tempTransConfig.sourceString,
                                source = activityVM.tempTransConfig.sourceLanguage,
                                target = activityVM.tempTransConfig.targetLanguage
                            ).also {
                                // 清空临时翻译参数
                                activityVM.tempTransConfig.clear()
                            }
                        }
                        navigation(
                            startDestination = TranslateScreen.SettingScreen.route,
                            route = "nav_composition_setting"
                        ) {
                            composable(TranslateScreen.SettingScreen.route) {
                                SettingsScreen()
                            }
                            val animDuration = 700
                            composable(
                                TranslateScreen.AboutScreen.route,
                                enterTransition = {
                                    slideIntoContainer(AnimatedContentScope.SlideDirection.Left, animationSpec = tween(animDuration))
                                },
                                exitTransition = {
                                    slideOutOfContainer(AnimatedContentScope.SlideDirection.Left, animationSpec = tween(animDuration))
                                },
                                popEnterTransition = {
                                    slideIntoContainer(AnimatedContentScope.SlideDirection.Right, animationSpec = tween(animDuration))
                                },
                                popExitTransition = {
                                    slideOutOfContainer(AnimatedContentScope.SlideDirection.Right, animationSpec = tween(animDuration))
                                }
                            ) {
                                AboutScreen()
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