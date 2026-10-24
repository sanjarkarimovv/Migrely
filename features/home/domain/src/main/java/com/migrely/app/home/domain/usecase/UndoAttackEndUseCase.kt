package com.migrely.app.home.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.home.domain.model.ReopenResultDomainModel
import com.migrely.app.home.domain.model.UndoResultDomainModel
import com.migrely.app.home.domain.repository.ReopenAttackRepository

/** Reopens a just-ended attack, unless another attack is in progress by now. */
class UndoAttackEndUseCase(
    private val reopenAttackRepository: ReopenAttackRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<String, UndoResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: String): UndoResultDomainModel =
        when (reopenAttackRepository.reopenAttack(request)) {
            ReopenResultDomainModel.Reopened -> UndoResultDomainModel.Undone
            ReopenResultDomainModel.AnotherInProgress -> UndoResultDomainModel.NotPossible
            ReopenResultDomainModel.StorageFull -> UndoResultDomainModel.Failed
            ReopenResultDomainModel.Failed -> UndoResultDomainModel.Failed
        }
}
