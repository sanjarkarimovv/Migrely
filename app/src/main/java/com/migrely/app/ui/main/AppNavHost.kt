package com.migrely.app.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
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
import com.migrely.app.navigation.mapper.PaywallOriginUiMapper
import com.migrely.app.onboarding.ui.view.Onboarding
import com.migrely.app.reminders.ui.view.DailyReminderOffer
import com.migrely.app.reminders.ui.view.Reminders
import com.migrely.app.report.ui.view.Report
import com.migrely.app.review.ui.view.ReviewPrompt
import com.migrely.app.settings.ui.view.AboutMigrely
import com.migrely.app.settings.ui.view.DeleteAllData
import com.migrely.app.settings.ui.view.DeleteAllDataObserver
import com.migrely.app.settings.ui.view.Licences
import com.migrely.app.settings.ui.view.Privacy
import com.migrely.app.settings.ui.view.Settings
import com.migrely.app.settings.ui.view.WarningSigns
import com.migrely.app.subscription.ui.view.Paywall
import com.migrely.app.subscription.ui.view.PaywallGate
import com.migrely.app.subscription.ui.view.PaywallTrigger
import com.migrely.app.subscription.ui.view.PremiumSection
import com.migrely.app.subscription.ui.view.TrialBanner
import com.migrely.app.ui.main.di.AppNavHostDependencies
import com.migrely.app.ui.main.route.AboutMigrely
import com.migrely.app.ui.main.route.AddPastAttack
import com.migrely.app.ui.main.route.AttackDetails
import com.migrely.app.ui.main.route.DeleteAllData
import com.migrely.app.ui.main.route.EditMedication
import com.migrely.app.ui.main.route.History
import com.migrely.app.ui.main.route.Home
import com.migrely.app.ui.main.route.Insights
import com.migrely.app.ui.main.route.Licences
import com.migrely.app.ui.main.route.LogMedication
import com.migrely.app.ui.main.route.Medications
import com.migrely.app.ui.main.route.Onboarding
import com.migrely.app.ui.main.route.Paywall
import com.migrely.app.ui.main.route.PaywallGate
import com.migrely.app.ui.main.route.Privacy
import com.migrely.app.ui.main.route.Reminders
import com.migrely.app.ui.main.route.Report
import com.migrely.app.ui.main.route.Settings
import com.migrely.app.ui.main.route.WarningSigns
import com.migrely.app.ui.main.route.YourData
import java.time.LocalDate

private val paywallOriginUiMapper = PaywallOriginUiMapper()

@Composable
fun AppNavHostDependencies.AppNavHost(
    navController: NavHostController,
    startDestination: Any,
    modifier: Modifier = Modifier
) {
    deleteAllDataDependencies.DeleteAllDataObserver(navController)
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        screen<Home>(navController) { entry ->
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
                },
                // And the subscription's trial reminder and 5th-migraine paywall (BRD D110, D111).
                trialBanner = { refreshKey -> trialBannerDependencies.TrialBanner(refreshKey) },
                paywallTrigger = { refreshKey ->
                    paywallTriggerDependencies.PaywallTrigger(navController, refreshKey)
                },
                // And the review feature's request for a rating (BRD D130, D132).
                reviewPrompt = { refreshKey -> reviewPromptDependencies.ReviewPrompt(refreshKey) }
            )
        }
        screen<AttackDetails>(navController) { entry ->
            val route = entry.toRoute<AttackDetails>()
            attackDetailsDependencies.AttackDetails(
                attackId = route.attackId,
                announceMerge = route.announceMerge,
                navController = navController
            )
        }
        screen<AddPastAttack>(navController) { entry ->
            addPastAttackDependencies.AddPastAttack(
                navController,
                day = entry.toRoute<AddPastAttack>().epochDay?.let(LocalDate::ofEpochDay)
            )
        }
        screen<LogMedication>(navController) { entry ->
            val route = entry.toRoute<LogMedication>()
            logMedicationDependencies.LogMedication(
                navController,
                attackId = route.attackId,
                darkRoom = route.darkRoom
            )
        }
        screen<Medications>(navController) {
            medicationsDependencies.Medications(navController)
        }
        screen<EditMedication>(navController) { entry ->
            editMedicationDependencies.EditMedication(
                navController,
                medicationId = entry.toRoute<EditMedication>().medicationId
            )
        }
        screen<Reminders>(navController) {
            remindersDependencies.Reminders(navController)
        }
        screen<Onboarding>(navController) {
            onboardingDependencies.Onboarding(
                navController,
                // Onboarding offers the reminders feature's daily reminder without knowing it (BRD D80).
                reminderOffer = { onDone ->
                    dailyReminderOfferDependencies.DailyReminderOffer(onDone)
                }
            )
        }
        screen<Report>(navController) {
            reportDependencies.Report(navController)
        }
        screen<Insights>(navController) {
            insightsDependencies.Insights(navController)
        }
        screen<Paywall>(navController) { entry ->
            paywallDependencies.Paywall(
                navController,
                context = paywallOriginUiMapper.toUi(entry.toRoute<Paywall>().origin)
            )
        }
        screen<PaywallGate>(navController) {
            paywallGateDependencies.PaywallGate(navController)
        }
        screen<Settings>(navController) {
            settingsDependencies.Settings(
                navController,
                // Settings shows the subscription's Premium section first (BRD D112).
                premiumSection = { premiumSectionDependencies.PremiumSection(navController) }
            )
        }
        screen<WarningSigns>(navController) {
            settingsPageDependencies.WarningSigns(navController)
        }
        screen<AboutMigrely>(navController) {
            settingsPageDependencies.AboutMigrely(navController)
        }
        screen<Privacy>(navController) {
            settingsPageDependencies.Privacy(navController)
        }
        screen<Licences>(navController) {
            settingsPageDependencies.Licences(navController)
        }
        screen<DeleteAllData>(navController) {
            deleteAllDataDependencies.DeleteAllData()
        }
        screen<YourData>(navController) {
            yourDataDependencies.YourData(navController)
        }
        screen<History>(navController) { entry ->
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
