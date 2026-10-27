package com.migrely.app.home.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.home.domain.model.ReopenResultDomainModel
import com.migrely.app.home.domain.model.UndoResultDomainModel
import com.migrely.app.home.domain.repository.ReopenAttackRepository

/** Reopens a just-ended attack, unless it would now overlap another attack (BRD EC-ATK-20). */
class UndoAttackEndUseCase(
    private val reopenAttackRepository: ReopenAttackRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<String, UndoResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: String): UndoResultDomainModel =
        when (reopenAttackRepository.reopenAttack(request)) {
            ReopenResultDomainModel.Reopened -> UndoResultDomainModel.Undone
            ReopenResultDomainModel.NotPossible -> UndoResultDomainModel.NotPossible
            ReopenResultDomainModel.StorageFull -> UndoResultDomainModel.Failed
            ReopenResultDomainModel.Failed -> UndoResultDomainModel.Failed
        }
}
