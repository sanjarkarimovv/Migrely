package com.migrely.app.history.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.history.domain.model.UndoResultDomainModel
import com.migrely.app.history.domain.repository.RestoreDeletedAttackRepository

/**
 * Undoes a deletion made on the details screen opened from History (BRD EC-HIS-04, D31). The request is the attack id.
 * Not possible when the attack would overlap another one or be a second attack in progress.
 */
class RestoreDeletedAttackUseCase(
    private val restoreDeletedAttackRepository: RestoreDeletedAttackRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<String, UndoResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: String): UndoResultDomainModel =
        restoreDeletedAttackRepository.restoreDeletedAttack(request)
}
