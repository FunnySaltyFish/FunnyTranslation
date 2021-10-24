package com.funny.translation.codeeditor.ui

import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
        composable(Screen.ScreenCodeEditor.route){
            ComposeCodeEditor(navController = navController,activityViewModel = activityCodeViewModel)
            deepLink("funny-trans://code_runner")
        }
        composable(Screen.ScreenCodeRunner.route){
            ComposeCodeRunner(navController = navController,activityCodeViewModel = activityCodeViewModel)
        }
    }
}

