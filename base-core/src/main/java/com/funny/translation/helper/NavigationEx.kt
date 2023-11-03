package com.funny.translation.helper

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val NAV_ANIM_DURATION = 500

fun NavGraphBuilder.animateComposable(
    route: String,
    animDuration: Int = NAV_ANIM_DURATION,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(animDuration)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(animDuration)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(animDuration)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(animDuration)
            )
        }
    ) {
        content(it)
    }
}

/**
 * initially from https://github.com/kiwicom/navigation-compose-typed/blob/main/core/src/main/kotlin/com/kiwi/navigationcompose/typed/ResultSharing.kt
 * Implementation of ResultEffect.
 */
@Composable
inline fun <reified R : Any> ResultEffect(
    navController: NavController,
    resultKey: String,
    crossinline block: (R) -> Unit,
) {
    DisposableEffect(navController) {
        // The implementation is based on the official documentation of the Result sharing.
        // It takes into consideration the possibility of a dialog usage (see the docs).
        // https://developer.android.com/guide/navigation/navigation-programmatic#additional_considerations
        val backStackEntry = navController.currentBackStackEntry ?: return@DisposableEffect onDispose {}
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && backStackEntry.savedStateHandle.contains(resultKey)) {
                val result = backStackEntry.savedStateHandle.remove<R>(resultKey)!!
                block(result)
            }
        }
        backStackEntry.lifecycle.addObserver(observer)
        onDispose {
            backStackEntry.lifecycle.removeObserver(observer)
        }
    }
}