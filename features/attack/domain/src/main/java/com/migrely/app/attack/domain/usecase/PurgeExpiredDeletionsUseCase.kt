package com.migrely.app.attack.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.attack.domain.repository.PurgeExpiredDeletionsRepository
import com.migrely.app.coroutine.CoroutineContextProvider

/**
 * Run when the app starts, so a deleted attack whose undo has run out never lingers hidden until the next deletion
 * (BRD D23, D118).
 */
class PurgeExpiredDeletionsUseCase(
    private val purgeExpiredDeletionsRepository: PurgeExpiredDeletionsRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, Unit>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit) =
        purgeExpiredDeletionsRepository.purgeExpiredDeletions()
}
