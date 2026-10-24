package com.migrely.app.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.migrely.app.attack.ui.view.AddPastAttack
import com.migrely.app.attack.ui.view.AttackDetails
import com.migrely.app.home.ui.view.Home
import com.migrely.app.ui.main.di.AppNavHostDependencies
import com.migrely.app.ui.main.route.AddPastAttack
import com.migrely.app.ui.main.route.AttackDetails
import com.migrely.app.ui.main.route.Home

@Composable
fun AppNavHostDependencies.AppNavHost(
    navController: NavHostController,
    startDestination: Any,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable<Home> { entry ->
            homeDependencies.Home(
                navController,
                startAttackRequested = entry.toRoute<Home>().startAttack
            )
        }
        composable<AttackDetails> { entry ->
            val route = entry.toRoute<AttackDetails>()
            attackDetailsDependencies.AttackDetails(
                attackId = route.attackId,
                announceMerge = route.announceMerge,
                navController = navController
            )
        }
        composable<AddPastAttack> {
            addPastAttackDependencies.AddPastAttack(navController)
        }
    }
}
