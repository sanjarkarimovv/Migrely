package com.migrely.app.settings.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.settings.domain.model.SettingsWriteResultDomainModel
import com.migrely.app.settings.domain.repository.SetPeriodTrackingRepository

/** Turns "Track periods" on or off; marks already saved stay (BRD DAY-F1, D79). */
class SetPeriodTrackingUseCase(
    private val setPeriodTrackingRepository: SetPeriodTrackingRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Boolean, SettingsWriteResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: Boolean): SettingsWriteResultDomainModel =
        setPeriodTrackingRepository.setPeriodTracking(request)
}
