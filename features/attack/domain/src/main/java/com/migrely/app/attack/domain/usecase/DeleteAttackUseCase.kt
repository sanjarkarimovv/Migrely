package com.migrely.app.attack.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import com.migrely.app.attack.domain.repository.DeleteAttackRepository
import com.migrely.app.coroutine.CoroutineContextProvider

/** Deletes an attack without a dialog (BRD ATK-F7). The request is the attack id; home offers the undo (D23). */
class DeleteAttackUseCase(
    private val deleteAttackRepository: DeleteAttackRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<String, WriteResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: String): WriteResultDomainModel =
        deleteAttackRepository.deleteAttack(request)
}
