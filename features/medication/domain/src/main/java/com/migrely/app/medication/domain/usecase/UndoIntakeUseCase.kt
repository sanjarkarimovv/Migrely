package com.migrely.app.medication.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.medication.domain.model.WriteResultDomainModel
import com.migrely.app.medication.domain.repository.DeleteIntakeRepository

/** Removes an intake just logged, from the confirmation's "Undo" (BRD D34). The request is the intake id. */
class UndoIntakeUseCase(
    private val deleteIntakeRepository: DeleteIntakeRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<String, WriteResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: String): WriteResultDomainModel =
        deleteIntakeRepository.deleteIntake(request)
}
