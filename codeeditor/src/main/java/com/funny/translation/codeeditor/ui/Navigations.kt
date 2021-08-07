package com.funny.translation.codeeditor.ui

import androidx.compose.runtime.Composable

const val NAV_EDITOR = "nav_editor"
sealed class Screen(val route:String){
    class ScreenCodeEditor() : Screen("nav_code_editor")
    class ScreenCodeRunner() : Screen("nav_code_runner")
}

@Composable
fun ComposeNavigation(){
    val navHost = rememberNavController()
}

