package com.funny.translation.translate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.funny.translation.trans.initLanguageDisplay
import com.funny.translation.translate.ui.main.MainScreen
import com.funny.translation.translate.ui.plugin.PluginScreen
import com.funny.translation.translate.ui.screen.TranslateScreen
import com.funny.translation.translate.ui.settings.SettingsScreen
import com.funny.translation.translate.ui.theme.TransTheme
import com.funny.translation.translate.ui.widget.CustomNavigation

@ExperimentalAnimationApi
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var currentScreen : TranslateScreen by remember {
        mutableStateOf(TranslateScreen.MainScreen)
    }
    TransTheme {
        Scaffold(
            bottomBar = {
                CustomNavigation(
                    screens = arrayOf(
                        TranslateScreen.MainScreen,
                        TranslateScreen.PluginScreen,
                        TranslateScreen.SettingScreen
                    ),
                    currentScreen = currentScreen
                ) { screen ->
                    currentScreen = screen
                    navController.navigate(currentScreen.route){

                        launchSingleTop = true
                    }
                }
            }
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