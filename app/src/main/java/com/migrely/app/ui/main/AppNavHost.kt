package com.migrely.app.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.migrely.app.attack.ui.view.AddPastAttack
import com.migrely.app.attack.ui.view.AttackDetails
import com.migrely.app.backup.ui.view.YourData
import com.migrely.app.history.ui.view.History
import com.migrely.app.history.ui.view.MonthSky
import com.migrely.app.home.ui.view.Home
import com.migrely.app.insights.ui.view.Insights
import com.migrely.app.medication.ui.view.EditMedication
import com.migrely.app.medication.ui.view.LogMedication
import com.migrely.app.medication.ui.view.MedicationPrompt
import com.migrely.app.medication.ui.view.Medications
import com.migrely.app.medication.ui.view.OveruseIndicator
import com.migrely.app.onboarding.ui.view.Onboarding
import com.migrely.app.reminders.ui.view.DailyReminderOffer
import com.migrely.app.reminders.ui.view.Reminders
import com.migrely.app.report.ui.view.Report
import com.migrely.app.settings.ui.view.Settings
import com.migrely.app.ui.main.di.AppNavHostDependencies
import com.migrely.app.ui.main.route.AddPastAttack
import com.migrely.app.ui.main.route.AttackDetails
import com.migrely.app.ui.main.route.EditMedication
import com.migrely.app.ui.main.route.History
import com.migrely.app.ui.main.route.Home
import com.migrely.app.ui.main.route.Insights
import com.migrely.app.ui.main.route.LogMedication
import com.migrely.app.ui.main.route.Medications
import com.migrely.app.ui.main.route.Onboarding
import com.migrely.app.ui.main.route.Reminders
import com.migrely.app.ui.main.route.Report
import com.migrely.app.ui.main.route.Settings
import com.migrely.app.ui.main.route.YourData
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
                // Home hosts History's month card and the medication card without knowing either feature
                // (change add-attack-history design D1, BRD D46).
                monthCalendar = { refreshKey ->
                    monthSkyDependencies.MonthSky(navController, refreshKey)
                },
                medicationPrompt = { refreshKey ->
                    medicationPromptDependencies.MedicationPrompt(refreshKey)
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
        composable<Reminders> {
            remindersDependencies.Reminders(navController)
        }
        composable<Onboarding> {
            onboardingDependencies.Onboarding(
                navController,
                // Onboarding offers the reminders feature's daily reminder without knowing it (BRD D80).
                reminderOffer = { onDone ->
                    dailyReminderOfferDependencies.DailyReminderOffer(onDone)
                }
            )
        }
        composable<Report> {
            reportDependencies.Report(navController)
        }
        composable<Insights> {
            insightsDependencies.Insights(navController)
        }
        composable<Settings> {
            settingsDependencies.Settings(navController)
        }
        composable<YourData> {
            yourDataDependencies.YourData(navController)
        }
        composable<History> { entry ->
            historyDependencies.History(
                navController,
                initialDay = entry.toRoute<History>().epochDay?.let(LocalDate::ofEpochDay),
                // History hosts the medication feature's standing overuse indicator without knowing it (BRD D59).
                overuseIndicator = { refreshKey ->
                    overuseIndicatorDependencies.OveruseIndicator(refreshKey)
                }
            )
        }
    }
}
