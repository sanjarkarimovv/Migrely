package com.migrely.app.home.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.home.domain.model.AttackLookupResultDomainModel
import com.migrely.app.home.domain.model.UndoResultDomainModel
import com.migrely.app.home.domain.model.WriteResultDomainModel
import com.migrely.app.home.domain.repository.DeleteAttackRepository
import com.migrely.app.home.domain.repository.GetAttackInProgressRepository

/** Removes a just-started attack. Only an attack that is still in progress can be removed this way. */
class UndoAttackStartUseCase(
    private val getAttackInProgressRepository: GetAttackInProgressRepository,
    private val deleteAttackRepository: DeleteAttackRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<String, UndoResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: String): UndoResultDomainModel =
        when (val lookup = getAttackInProgressRepository.attackInProgress()) {
            is AttackLookupResultDomainModel.InProgress ->
                if (lookup.attack.id == request) {
                    deleteAttackRepository.deleteAttack(request).toUndoResult()
                } else {
                    UndoResultDomainModel.NotPossible
                }

            AttackLookupResultDomainModel.None -> UndoResultDomainModel.NotPossible
            AttackLookupResultDomainModel.Failed -> UndoResultDomainModel.Failed
        }

    private fun WriteResultDomainModel.toUndoResult() = when (this) {
        WriteResultDomainModel.Written -> UndoResultDomainModel.Undone
        WriteResultDomainModel.StorageFull -> UndoResultDomainModel.Failed
        WriteResultDomainModel.Failed -> UndoResultDomainModel.Failed
    }
}
