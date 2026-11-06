package com.migrely.app.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

/**
 * One destination of the nav host. A screen stays composed through its exit fade, so its touches
 * are ignored from the moment another entry is current (issue #142). The box hands the screen the
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
