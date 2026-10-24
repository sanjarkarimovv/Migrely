package com.migrely.app.home.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.home.domain.model.WriteResultDomainModel
import com.migrely.app.home.domain.repository.GetCurrentTimeRepository
import com.migrely.app.home.domain.repository.SetPeriodRepository

/** Home's one-tap period toggle: marks or unmarks today, the device's date (BRD DAY-F1, D48). The request is the mark. */
class SetPeriodTodayUseCase(
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val setPeriodRepository: SetPeriodRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Boolean, WriteResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: Boolean): WriteResultDomainModel {
        val now = getCurrentTimeRepository.currentTime()
        return setPeriodRepository.setPeriod(now.instant.atZone(now.zone).toLocalDate(), request)
    }
}
