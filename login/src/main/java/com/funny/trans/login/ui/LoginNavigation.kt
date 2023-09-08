package com.funny.trans.login.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.funny.translation.bean.UserInfoBean
import com.funny.translation.ui.animatedGradientBackground

sealed class LoginRoute(val route: String) {
    object LoginPage: LoginRoute("login_page")
    object ResetPasswordPage: LoginRoute("reset_password")
    object FindUsernamePage: LoginRoute("find_user_name")
    object ChangeUsernamePage: LoginRoute("change_user_name")
    object CancelAccountPage: LoginRoute("cancel_account")
}

@Composable
fun LoginNavigation(
    onLoginSuccess: (UserInfoBean) -> Unit,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = LoginRoute.LoginPage.route,
        modifier = Modifier
            .fillMaxSize()
            .animatedGradientBackground(
                 MaterialTheme.colorScheme.surface,
                 MaterialTheme.colorScheme.tertiaryContainer,
            )
            .statusBarsPadding(),
    ){
        addLoginRoutes(navController, onLoginSuccess = onLoginSuccess)
    }
}

fun NavGraphBuilder.addLoginRoutes(
    navController: NavHostController,
    onLoginSuccess: (UserInfoBean) -> Unit,
){
    animateComposable(LoginRoute.LoginPage.route){
        LoginPage(navController = navController, onLoginSuccess = onLoginSuccess)
    }
    animateComposable(LoginRoute.ResetPasswordPage.route){
        ResetPasswordPage(navController = navController)
    }
    animateComposable(LoginRoute.FindUsernamePage.route){
        FindUsernamePage()
    }
    animateComposable(LoginRoute.ChangeUsernamePage.route){
        ChangeUsernamePage(navController = navController)
    }
    animateComposable(LoginRoute.CancelAccountPage.route){
        CancelAccountPage(navController = navController)
    }
}


private fun NavGraphBuilder.animateComposable(
    route: String,
    animDuration: Int = 400,
    content: @Composable () -> Unit,
) {
    composable(
        route,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(animDuration)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(animDuration)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = tween(animDuration)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = tween(animDuration)
            )
        }
    ) {
        content()
    }
}