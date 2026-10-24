package com.migrely.app.attack.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.attack.domain.model.SetIntakeEffectRequestDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import com.migrely.app.attack.domain.repository.SetIntakeEffectRepository
import com.migrely.app.coroutine.CoroutineContextProvider

/** Sets or changes how much a medication helped, at any time; home stops asking about it (BRD EC-MED-12, D39). */
class SetIntakeEffectUseCase(
    private val setIntakeEffectRepository: SetIntakeEffectRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<SetIntakeEffectRequestDomainModel, WriteResultDomainModel>(
    coroutineContextProvider
) {
    override fun executeInBackground(
        request: SetIntakeEffectRequestDomainModel
    ): WriteResultDomainModel =
        setIntakeEffectRepository.setIntakeEffect(request.intakeId, request.effect)
}
