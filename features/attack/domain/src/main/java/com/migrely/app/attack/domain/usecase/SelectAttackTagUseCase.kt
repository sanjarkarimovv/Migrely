package com.migrely.app.attack.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.attack.domain.model.SelectAttackTagRequestDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import com.migrely.app.attack.domain.repository.SelectAttackTagRepository
import com.migrely.app.coroutine.CoroutineContextProvider

/** Selects or clears one location, symptom or trigger. Repeating a request changes nothing, so quick taps are safe. */
class SelectAttackTagUseCase(
    private val selectAttackTagRepository: SelectAttackTagRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<
    SelectAttackTagRequestDomainModel,
    WriteResultDomainModel
    >(coroutineContextProvider) {
    override fun executeInBackground(
        request: SelectAttackTagRequestDomainModel
    ): WriteResultDomainModel =
        selectAttackTagRepository.selectAttackTag(request.attackId, request.tag, request.selected)
}
