package com.migrely.app.attack.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.attack.domain.model.PastAttackDraftDomainModel
import com.migrely.app.attack.domain.repository.GetCurrentTimeRepository
import com.migrely.app.coroutine.CoroutineContextProvider
import java.time.Duration
import java.time.temporal.ChronoUnit

private val DEFAULT_DURATION: Duration = Duration.ofHours(2)

/** A new past attack starts as two hours ago to now, rounded down to the minute, in the device's zone (BRD D26). */
class GetPastAttackDraftUseCase(
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, PastAttackDraftDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit): PastAttackDraftDomainModel {
        val now = getCurrentTimeRepository.currentTime()
        val end = now.instant.truncatedTo(ChronoUnit.MINUTES)
        return PastAttackDraftDomainModel(end.minus(DEFAULT_DURATION), end, now.zone)
    }
}
