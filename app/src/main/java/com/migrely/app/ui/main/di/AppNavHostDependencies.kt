package com.migrely.app.ui.main.di

import com.migrely.app.attack.ui.di.AttackDetailsDependencies
import com.migrely.app.home.ui.di.HomeDependencies

data class AppNavHostDependencies(
    val homeDependencies: HomeDependencies,
    val attackDetailsDependencies: AttackDetailsDependencies
)
