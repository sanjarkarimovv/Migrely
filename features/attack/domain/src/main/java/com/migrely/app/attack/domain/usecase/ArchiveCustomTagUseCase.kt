package com.migrely.app.attack.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import com.migrely.app.attack.domain.repository.SetCustomTagArchivedRepository
import com.migrely.app.coroutine.CoroutineContextProvider

/**
 * Removes a custom tag from the user's list (BRD EC-ATK-11). The request is the tag id. The tag stays on the attacks
 * that use it.
 */
class ArchiveCustomTagUseCase(
    private val setCustomTagArchivedRepository: SetCustomTagArchivedRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<String, WriteResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: String): WriteResultDomainModel =
        setCustomTagArchivedRepository.setCustomTagArchived(request, archived = true)
}
