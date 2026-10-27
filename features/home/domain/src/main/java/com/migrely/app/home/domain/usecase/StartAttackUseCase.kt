package com.migrely.app.home.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.home.domain.model.StartAttackResultDomainModel
import com.migrely.app.home.domain.repository.GetCurrentTimeRepository
import com.migrely.app.home.domain.repository.StartAttackRepository

class StartAttackUseCase(
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val startAttackRepository: StartAttackRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, StartAttackResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit): StartAttackResultDomainModel {
        val now = getCurrentTimeRepository.currentTime()
        return startAttackRepository.startAttackIfNoneInProgress(
            start = now.instant,
            zone = now.zone
        )
    }
}
