package com.migrely.app.premium

import com.migrely.app.subscription.domain.usecase.RefreshPremiumUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** The background check's needs: WorkManager creates the worker, which reaches the graph here. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface PremiumEntryPoint {
    fun refreshPremiumUseCase(): RefreshPremiumUseCase
}
