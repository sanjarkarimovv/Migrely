package com.migrely.app.medication.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.medication.domain.model.AcknowledgeOveruseRequestDomainModel
import com.migrely.app.medication.domain.model.WriteResultDomainModel
import com.migrely.app.medication.domain.repository.AcknowledgeOveruseRepository
import com.migrely.app.medication.domain.repository.GetCurrentTimeRepository

/** "Got it" on an overuse warning: that category stays quiet at that level or lower for 30 days (BRD EC-MED-06). */
class AcknowledgeOveruseUseCase(
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val acknowledgeOveruseRepository: AcknowledgeOveruseRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<AcknowledgeOveruseRequestDomainModel, WriteResultDomainModel>(
    coroutineContextProvider
) {
    override fun executeInBackground(
        request: AcknowledgeOveruseRequestDomainModel
    ): WriteResultDomainModel = acknowledgeOveruseRepository.acknowledgeOveruse(
        request.category,
        request.level,
        getCurrentTimeRepository.currentTime().instant
    )
}
