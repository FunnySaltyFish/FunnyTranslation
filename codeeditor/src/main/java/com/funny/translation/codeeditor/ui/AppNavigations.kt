package com.funny.translation.codeeditor.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.funny.translation.codeeditor.ui.editor.ComposeCodeEditor
import com.funny.translation.codeeditor.ui.runner.ComposeCodeRunner
import com.funny.translation.codeeditor.vm.ActivityCodeViewModel

const val TAG = "AppNav"
sealed class Screen(val route:String){
    object ScreenCodeEditor : Screen("nav_code_editor")
    object ScreenCodeRunner : Screen("nav_code_runner")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val activityCodeViewModel : ActivityCodeViewModel = viewModel()
    NavHost(
        navController = navController,
        startDestination = Screen.ScreenCodeEditor.route
    ){
        composable(
            Screen.ScreenCodeEditor.route,
            deepLinks = listOf(navDeepLink { uriPattern = "funny-trans://code_editor" })
        ){
            ComposeCodeEditor(navController = navController,activityViewModel = activityCodeViewModel)
        }
        composable(Screen.ScreenCodeRunner.route){
            ComposeCodeRunner(navController = navController,activityCodeViewModel = activityCodeViewModel)
        }
    }
}

