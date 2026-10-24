package com.migrely.app.backup.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.backup.domain.model.AutomaticCopyDomainModel
import com.migrely.app.backup.domain.repository.GetAutomaticCopyRepository
import com.migrely.app.coroutine.CoroutineContextProvider

/** When the newest automatic copy was made and what it holds, so Your data can offer it (BRD D98). */
class GetAutomaticCopyUseCase(
    private val getAutomaticCopyRepository: GetAutomaticCopyRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, AutomaticCopyDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit): AutomaticCopyDomainModel =
        getAutomaticCopyRepository.automaticCopy()
}
