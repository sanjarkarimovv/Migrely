package com.migrely.app.attack.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.attack.domain.model.RemoveIntensityPointRequestDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import com.migrely.app.attack.domain.repository.RemoveIntensityPointRepository
import com.migrely.app.coroutine.CoroutineContextProvider

class RemoveIntensityPointUseCase(
    private val removeIntensityPointRepository: RemoveIntensityPointRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<
    RemoveIntensityPointRequestDomainModel,
    WriteResultDomainModel
    >(coroutineContextProvider) {
    override fun executeInBackground(
        request: RemoveIntensityPointRequestDomainModel
    ): WriteResultDomainModel =
        removeIntensityPointRepository.removeIntensityPoint(request.attackId, request.point)
}
