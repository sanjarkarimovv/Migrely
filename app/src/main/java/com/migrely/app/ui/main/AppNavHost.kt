package com.migrely.app.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.migrely.app.attack.ui.view.AddPastAttack
import com.migrely.app.attack.ui.view.AttackDetails
import com.migrely.app.history.ui.view.History
import com.migrely.app.history.ui.view.MonthSky
import com.migrely.app.home.ui.view.Home
import com.migrely.app.medication.ui.view.EditMedication
import com.migrely.app.medication.ui.view.LogMedication
import com.migrely.app.medication.ui.view.Medications
import com.migrely.app.ui.main.di.AppNavHostDependencies
import com.migrely.app.ui.main.route.AddPastAttack
import com.migrely.app.ui.main.route.AttackDetails
import com.migrely.app.ui.main.route.EditMedication
import com.migrely.app.ui.main.route.History
import com.migrely.app.ui.main.route.Home
import com.migrely.app.ui.main.route.LogMedication
import com.migrely.app.ui.main.route.Medications
import java.time.LocalDate

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
                startAttackRequested = entry.toRoute<Home>().startAttack,
                // Home hosts History's month card without knowing the history feature (design D1).
                monthCalendar = { refreshKey ->
                    monthSkyDependencies.MonthSky(navController, refreshKey)
                }
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
        composable<AddPastAttack> { entry ->
            addPastAttackDependencies.AddPastAttack(
                navController,
                day = entry.toRoute<AddPastAttack>().epochDay?.let(LocalDate::ofEpochDay)
            )
        }
        composable<LogMedication> { entry ->
            val route = entry.toRoute<LogMedication>()
            logMedicationDependencies.LogMedication(
                navController,
                attackId = route.attackId,
                darkRoom = route.darkRoom
            )
        }
        composable<Medications> {
            medicationsDependencies.Medications(navController)
        }
        composable<EditMedication> { entry ->
            editMedicationDependencies.EditMedication(
                navController,
                medicationId = entry.toRoute<EditMedication>().medicationId
            )
        }
        composable<History> { entry ->
            historyDependencies.History(
                navController,
                initialDay = entry.toRoute<History>().epochDay?.let(LocalDate::ofEpochDay)
            )
        }
    }
}
