package com.migrely.app.ui.main

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

private const val SCREEN_FADE_MILLIS = 200

/** Linear: at this length the default easing looks close to a cut. */
internal val SCREEN_FADE: FiniteAnimationSpec<Float> =
    tween(SCREEN_FADE_MILLIS, easing = LinearEasing)

/** The nav host and the one fade between all its screens (issue #148). */
@Composable
internal fun ScreenNavHost(
    navController: NavHostController,
    startDestination: Any,
    modifier: Modifier = Modifier,
    builder: NavGraphBuilder.() -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { fadeIn(SCREEN_FADE) },
        exitTransition = { fadeOut(SCREEN_FADE) },
        popEnterTransition = { fadeIn(SCREEN_FADE) },
        popExitTransition = { fadeOut(SCREEN_FADE) },
        builder = builder
    )
}

/**
 * One destination of the nav host. A screen stays composed through its exit fade, so the pointer
 * input of this window on it is ignored from the moment another entry is current (issue #142). A
 * dialog or popup is a window of its own and is not covered. The box hands the screen the
 * constraints the host gave, so no screen changes its size or place.
 */
internal inline fun <reified ROUTE : Any> NavGraphBuilder.screen(
    navController: NavController,
    noinline content: @Composable (NavBackStackEntry) -> Unit
) {
    composable<ROUTE> { entry ->
        Box(
            modifier = Modifier.touchesOnlyWhile {
                navController.currentBackStackEntry?.id == entry.id
            },
            propagateMinConstraints = true
        ) {
            content(entry)
        }
    }
}
