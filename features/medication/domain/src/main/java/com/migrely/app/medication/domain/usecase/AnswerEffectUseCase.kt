package com.migrely.app.medication.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.medication.domain.model.AnswerEffectRequestDomainModel
import com.migrely.app.medication.domain.model.WriteResultDomainModel
import com.migrely.app.medication.domain.repository.SetIntakeEffectRepository

/** Stores the answer to "Did it help?" and closes the question (BRD MED-F4, D39). */
class AnswerEffectUseCase(
    private val setIntakeEffectRepository: SetIntakeEffectRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<AnswerEffectRequestDomainModel, WriteResultDomainModel>(
    coroutineContextProvider
) {
    override fun executeInBackground(
        request: AnswerEffectRequestDomainModel
    ): WriteResultDomainModel =
        setIntakeEffectRepository.setIntakeEffect(request.intakeId, request.effect)
}
