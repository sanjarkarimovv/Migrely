package com.migrely.app.attack.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.attack.domain.model.RestoreIntakeRequestDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import com.migrely.app.attack.domain.repository.RestoreIntakeRepository
import com.migrely.app.coroutine.CoroutineContextProvider

/** Undoes [RemoveIntakeUseCase] with the same intake (BRD EC-UX-16). */
class RestoreIntakeUseCase(
    private val restoreIntakeRepository: RestoreIntakeRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<RestoreIntakeRequestDomainModel, WriteResultDomainModel>(
    coroutineContextProvider
) {
    override fun executeInBackground(
        request: RestoreIntakeRequestDomainModel
    ): WriteResultDomainModel =
        restoreIntakeRepository.restoreIntake(request.attackId, request.intake)
}
