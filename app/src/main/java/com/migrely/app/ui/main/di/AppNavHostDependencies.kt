package com.migrely.app.ui.main.di

import com.migrely.app.attack.ui.di.AddPastAttackDependencies
import com.migrely.app.attack.ui.di.AttackDetailsDependencies
import com.migrely.app.history.ui.di.HistoryDependencies
import com.migrely.app.history.ui.di.MonthSkyDependencies
import com.migrely.app.home.ui.di.HomeDependencies
import com.migrely.app.medication.ui.di.EditMedicationDependencies
import com.migrely.app.medication.ui.di.LogMedicationDependencies
import com.migrely.app.medication.ui.di.MedicationsDependencies

data class AppNavHostDependencies(
    val homeDependencies: HomeDependencies,
    val attackDetailsDependencies: AttackDetailsDependencies,
    val addPastAttackDependencies: AddPastAttackDependencies,
    val historyDependencies: HistoryDependencies,
    val monthSkyDependencies: MonthSkyDependencies,
    val logMedicationDependencies: LogMedicationDependencies,
    val medicationsDependencies: MedicationsDependencies,
    val editMedicationDependencies: EditMedicationDependencies
)
