package com.funny.translation.translate

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.azhon.appupdate.utils.ApkUtil
import com.funny.jetsetting.core.LocalDataSave
import com.funny.translation.codeeditor.extensions.externalCache
import com.funny.translation.debug.Debug
import com.funny.translation.debug.DefaultDebugTarget
import com.funny.translation.trans.initLanguageDisplay
import com.funny.translation.translate.bean.Consts
import com.funny.translation.translate.database.DefaultData
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.ui.main.MainScreen
import com.funny.translation.translate.ui.plugin.PluginScreen
import com.funny.translation.translate.ui.screen.TranslateScreen
import com.funny.translation.translate.ui.settings.SettingsScreen
import com.funny.translation.translate.ui.thanks.ThanksScreen
import com.funny.translation.translate.ui.theme.TransTheme
import com.funny.translation.translate.ui.widget.CustomNavigation
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "AppNav"
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun AppNavigation(
    exitAppAction : ()->Unit
) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val activityVM : ActivityViewModel = viewModel()

    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    BackHandler(enabled = true) {
        if (navController.previousBackStackEntry == null){
            val curTime = System.currentTimeMillis()
            if(curTime - activityVM.lastBackTime > 2000){
                scope.launch { scaffoldState.snackbarHostState.showSnackbar(FunnyApplication.resources.getString(R.string.snack_quit)) }
                activityVM.lastBackTime = curTime
            }else{
                exitAppAction()
            }
        }else{
            Log.d(TAG, "AppNavigation: back")
            //currentScreen = TranslateScreen.MainScreen
        }
    }

    val systemUiController = rememberSystemUiController()
    // 分开设置，考虑到背景颜色，我们需要动态更新图标颜色嘛
    val darkIcon = MaterialTheme.colors.isLight
    val showStatusBar = LocalDataSave.current.readData(Consts.KEY_SHOW_STATUS_BAR, false)
    Log.d(TAG, "AppNavigation: currentStatusBar:$showStatusBar")
    if(showStatusBar){
        systemUiController.isStatusBarVisible = true
        systemUiController.setStatusBarColor(MaterialTheme.colors.background, darkIcons = darkIcon)
    }else {
        systemUiController.isStatusBarVisible = false
    }

    systemUiController.setNavigationBarColor(Color.Transparent, darkIcons = darkIcon)


    ProvideWindowInsets {
        TransTheme {
            Scaffold(
                bottomBar = {
                    val currentScreen  = navController.currentScreenAsState()
                    CustomNavigation(
                        screens = arrayOf(
                            TranslateScreen.MainScreen,
                            TranslateScreen.PluginScreen,
                            TranslateScreen.SettingScreen,
                            TranslateScreen.ThanksScreen
                        ),
                        currentScreen = currentScreen.value
                    ) { screen ->
                        if(screen == currentScreen)return@CustomNavigation

                        val currentRoute = navBackStackEntry?.destination?.route
                        Log.d(TAG, "AppNavigation: $currentRoute")

                        //currentScreen = screen
                        navController.navigate(screen.route){
                            //当底部导航导航到在非首页的页面时，执行手机的返回键 回到首页
                            popUpTo(navController.graph.startDestinationId){
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
                NavHost(
                    navController = navController,
                    startDestination = TranslateScreen.MainScreen.route,
                    modifier = Modifier.statusBarsPadding()
                ) {
                    composable(TranslateScreen.MainScreen.route) {
                        MainScreen { str ->
                            scope.launch {
                                scaffoldState.snackbarHostState.showSnackbar(str)
                            }
                        }
                    }
                    composable(TranslateScreen.SettingScreen.route) {
                        SettingsScreen()
                    }
                    composable(TranslateScreen.PluginScreen.route) {
                        PluginScreen(
                            showSnackbar = { str ->
                                scope.launch {
                                    scaffoldState.snackbarHostState.showSnackbar(str)
                                }
                            },navController = navController
                        )
                    }
                    composable(TranslateScreen.ThanksScreen.route) {
                        ThanksScreen()
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

class TransActivity : ComponentActivity() {
    private lateinit var activityViewModel: ActivityViewModel
    lateinit var context : Context

    @OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Debug.addTarget(DefaultDebugTarget)

        //WindowCompat.setDecorFitsSystemWindows(window, false)
        context = this
        activityViewModel = ViewModelProvider(this).get(ActivityViewModel::class.java)
        lifecycleScope.launch(Dispatchers.IO) {
            initLanguageDisplay(resources)
            if(appDB.jsDao.getJsCount() == 0) appDB.jsDao.insertJsList(DefaultData.getDefaultJsList(lifecycleScope))
        }

//        ScreenUtils.initStatusBar(this)
//        WindowCompat.setDecorFitsSystemWindows(window,false)

//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
//        )

//        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AppNavigation(
                exitAppAction = {
                    this.finish()
                }
            )
        }

        lifecycleScope.launch(Dispatchers.IO) {
            activityViewModel.checkUpdate(context)
            ApkUtil.deleteOldApk(context, context.externalCache.absolutePath + "/" + "update_apk.apk")
        }
    }
}