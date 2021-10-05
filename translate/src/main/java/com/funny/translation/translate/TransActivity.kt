package com.funny.translation.translate

import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.BottomNavigation
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.funny.translation.trans.initLanguageDisplay
import com.funny.translation.translate.ui.main.MainScreen
import com.funny.translation.translate.ui.plugin.PluginScreen
import com.funny.translation.translate.ui.screen.TranslateScreen
import com.funny.translation.translate.ui.settings.SettingsScreen
import com.funny.translation.translate.ui.theme.TransTheme
import com.funny.translation.translate.ui.widget.CustomNavigation
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

private const val TAG = "AppNav"
@ExperimentalAnimationApi
@Composable
fun AppNavigation() {
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
                exitProcess(0);
            }
        }else{
            Log.d(TAG, "AppNavigation: back")
            //currentScreen = TranslateScreen.MainScreen
        }
    }

    TransTheme {
        Scaffold(
            bottomBar = {
                val currentScreen  = navController.currentScreenAsState()
                CustomNavigation(
                    screens = arrayOf(
                        TranslateScreen.MainScreen,
                        TranslateScreen.PluginScreen,
                        TranslateScreen.SettingScreen
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
                startDestination = TranslateScreen.MainScreen.route
            ) {
                composable(TranslateScreen.MainScreen.route) {
                    MainScreen()
                }
                composable(TranslateScreen.SettingScreen.route) {
                    SettingsScreen()
                }
                composable(TranslateScreen.PluginScreen.route) {
                    PluginScreen()
                }
            }
        }
    }

}

@Stable
@Composable
private fun NavController.currentScreenAsState(): MutableState<TranslateScreen> {
    val selectedItem = remember { mutableStateOf<TranslateScreen>(TranslateScreen.MainScreen) }

    DisposableEffect(this) {
        var route : String? = ""

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

//                destination.hierarchy.any { it.route == Screen.Watched.route } -> {
//                    selectedItem.value = Screen.Watched
//                }
//                destination.hierarchy.any { it.route == Screen.Following.route } -> {
//                    selectedItem.value = Screen.Following
//                }
//                destination.hierarchy.any { it.route == Screen.Search.route } -> {
//                    selectedItem.value = Screen.Search
//                }
            }
        }
        addOnDestinationChangedListener(listener)

        onDispose {
            removeOnDestinationChangedListener(listener)
        }
    }

    return selectedItem
}

fun findScreenByRoute(route : String?) = when(route){
    TranslateScreen.MainScreen.route -> TranslateScreen.MainScreen
    TranslateScreen.PluginScreen.route -> TranslateScreen.PluginScreen
    TranslateScreen.SettingScreen.route -> TranslateScreen.SettingScreen
    else -> TranslateScreen.MainScreen
}

class TransActivity : ComponentActivity() {
    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initLanguageDisplay(resources)

        setContent {
            AppNavigation()
        }
    }
}