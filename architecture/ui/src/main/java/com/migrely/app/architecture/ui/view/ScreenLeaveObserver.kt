package com.migrely.app.architecture.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

/**
 * Calls [onLeft] when this screen stops being the top of [navController]'s back stack: Back, or forward to another
 * screen. It reads the back stack, not the lifecycle, because Home, Recents, the screen turning off and Android
 * Settings stop the screen too, while it stays on top; a dialog, a rotation and a process restore keep it on top as
 * well. It is told during the navigation itself, so what it changes is saved with the screen (BRD D165 (5)). Outside
 * a `NavHost` there is no back stack, so it never calls.
 */
@Composable
fun ScreenLeaveObserver(navController: NavController, onLeft: () -> Unit) {
    val screen = LocalLifecycleOwner.current
    val currentOnLeft by rememberUpdatedState(onLeft)
    DisposableEffect(navController, screen) {
        val listener = NavController.OnDestinationChangedListener { _, _, _ ->
            if (!navController.isCurrentScreen(screen)) currentOnLeft()
        }
        if (screen is NavBackStackEntry) navController.addOnDestinationChangedListener(listener)
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }
}

/**
 * Whether [screen] is the top of the back stack. Compared by id, because `launchSingleTop` replaces the top entry's
 * object and keeps its id. Outside a `NavHost` there is no back stack, so any screen is current.
 */
internal fun NavController.isCurrentScreen(screen: LifecycleOwner): Boolean {
    val entry = screen as? NavBackStackEntry ?: return true
    return currentBackStackEntry?.id == entry.id
}
