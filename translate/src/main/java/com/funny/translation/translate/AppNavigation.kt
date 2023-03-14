@file:OptIn(ExperimentalMaterial3Api::class)

package com.funny.translation.translate

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.funny.data_saver.core.LocalDataSaver
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.translation.AppConfig
import com.funny.translation.Consts
import com.funny.translation.bean.UserBean
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.toastOnUi
import com.funny.translation.theme.TransTheme
import com.funny.translation.translate.ui.main.MainScreen
import com.funny.translation.translate.ui.plugin.PluginScreen
import com.funny.translation.translate.ui.screen.TranslateScreen
import com.funny.translation.translate.ui.settings.*
import com.funny.translation.translate.ui.thanks.ThanksScreen
import com.funny.translation.translate.ui.thanks.TransProScreen
import com.funny.translation.translate.ui.thanks.addUserProfileRoutes
import com.funny.translation.translate.ui.widget.CustomNavigation
import com.funny.translation.translate.ui.widget.MarkdownText
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.navigation
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
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
    shouldJumpToMainContent: MutableState<Boolean>,
    exitAppAction: () -> Unit
) {
    val navController = rememberAnimatedNavController()

    LaunchedEffect(shouldJumpToMainContent.value){
        if (shouldJumpToMainContent.value){
            if (navController.currentBackStackEntry != null){
                navController.navigate(TranslateScreen.MainScreen.route){
                    launchSingleTop = true
                    popUpTo(TranslateScreen.MainScreen.route)
                }
            }
            shouldJumpToMainContent.value = false
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val activityVM: ActivityViewModel = viewModel()
    val context = LocalContext.current
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
                bottomBar = {
                    val currentScreen = navController.currentScreenAsState()
                    CustomNavigation(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
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
                        navController.navigateSingleTop(route = screen.route)
                    }
                },
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState)
                }
            ) {
                AnimatedNavHost(
                    navController = navController,
                    startDestination = TranslateScreen.MainScreen.route,
                    modifier = Modifier
                        .statusBarsPadding()
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
                    navigation(
                        startDestination = TranslateScreen.SettingScreen.route,
                        route = "nav_1_setting",
                    ) {
                        composable(TranslateScreen.SettingScreen.route) {
                            SettingsScreen()
                        }
                        animateComposable(
                            TranslateScreen.AboutScreen.route,
                        ) {
                            AboutScreen()
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

                    composable(TranslateScreen.PluginScreen.route) {
                        PluginScreen()
                    }
                    navigation(startDestination = TranslateScreen.ThanksScreen.route, route = "nav_1_thanks") {
                        composable(TranslateScreen.ThanksScreen.route){ ThanksScreen(navController) }
                        addUserProfileRoutes(
                            navHostController = navController,
                            onLoginSuccess = { userBean ->
                                Log.d(TAG, "登录成功: 用户: $userBean")
                                if (userBean.isValid()) AppConfig.login(userBean)
                            },
                            onResetPasswordSuccess = {
                                context.toastOnUi("修改密码成功，请重新登陆~")
                                AppConfig.logout()
                                navController.popBackStack(navController.graph.startDestinationId, false)
                            }
                        )
                        animateComposable(TranslateScreen.TransProScreen.route) {
                            TransProScreen()
                        }
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

fun NavHostController.navigateSingleTop(route: String, popUpToMain: Boolean = true){
    val navController = this
    navController.navigate(route) {
        //当底部导航导航到在非首页的页面时，执行手机的返回键 回到首页
        if (popUpToMain) {
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
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
    content: @Composable () -> Unit,
) {
    composable(
        route,
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
        content()
    }
}

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