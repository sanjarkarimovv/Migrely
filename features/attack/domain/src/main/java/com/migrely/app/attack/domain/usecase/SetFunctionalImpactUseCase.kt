package com.migrely.app.attack.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.attack.domain.model.SetFunctionalImpactRequestDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import com.migrely.app.attack.domain.repository.SetFunctionalImpactRepository
import com.migrely.app.coroutine.CoroutineContextProvider

class SetFunctionalImpactUseCase(
    private val setFunctionalImpactRepository: SetFunctionalImpactRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<
    SetFunctionalImpactRequestDomainModel,
    WriteResultDomainModel
    >(coroutineContextProvider) {
    override fun executeInBackground(
        request: SetFunctionalImpactRequestDomainModel
    ): WriteResultDomainModel =
        setFunctionalImpactRepository.setFunctionalImpact(request.attackId, request.impact)
}
