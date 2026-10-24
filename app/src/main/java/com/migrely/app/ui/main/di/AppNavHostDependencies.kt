package com.migrely.app.ui.main.di

import com.migrely.app.attack.ui.di.AddPastAttackDependencies
import com.migrely.app.attack.ui.di.AttackDetailsDependencies
import com.migrely.app.history.ui.di.HistoryDependencies
import com.migrely.app.history.ui.di.MonthSkyDependencies
import com.migrely.app.home.ui.di.HomeDependencies

data class AppNavHostDependencies(
    val homeDependencies: HomeDependencies,
    val attackDetailsDependencies: AttackDetailsDependencies,
    val addPastAttackDependencies: AddPastAttackDependencies,
    val historyDependencies: HistoryDependencies,
    val monthSkyDependencies: MonthSkyDependencies
)
