package com.migrely.app.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.migrely.app.home.ui.view.Home
import com.migrely.app.ui.main.di.AppNavHostDependencies
import com.migrely.app.ui.main.route.Home

@Composable
fun AppNavHostDependencies.AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: Any = Home
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable<Home> {
            homeDependencies.Home(navController)
        }
    }
}
