package com.migrely.app.attack.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import com.migrely.app.attack.domain.repository.RemoveIntakeRepository
import com.migrely.app.coroutine.CoroutineContextProvider

/** Removes a medication from the attack's details; the screen keeps it for "Undo" (BRD D34). Request: intake id. */
class RemoveIntakeUseCase(
    private val removeIntakeRepository: RemoveIntakeRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<String, WriteResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: String): WriteResultDomainModel =
        removeIntakeRepository.removeIntake(request)
}
