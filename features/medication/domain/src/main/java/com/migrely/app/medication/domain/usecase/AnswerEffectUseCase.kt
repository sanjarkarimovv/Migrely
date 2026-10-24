package com.migrely.app.medication.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.medication.domain.model.AnswerEffectRequestDomainModel
import com.migrely.app.medication.domain.model.WriteResultDomainModel
import com.migrely.app.medication.domain.repository.CancelEffectQuestionRepository
import com.migrely.app.medication.domain.repository.SetIntakeEffectRepository

/** Stores the answer to "Did it help?" and closes the question (BRD MED-F4, D39). */
class AnswerEffectUseCase(
    private val setIntakeEffectRepository: SetIntakeEffectRepository,
    private val cancelEffectQuestionRepository: CancelEffectQuestionRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<AnswerEffectRequestDomainModel, WriteResultDomainModel>(
    coroutineContextProvider
) {
    override fun executeInBackground(
        request: AnswerEffectRequestDomainModel
    ): WriteResultDomainModel =
        setIntakeEffectRepository.setIntakeEffect(request.intakeId, request.effect)
            .also {
                if (it ==
                    WriteResultDomainModel.Done
                ) {
                    cancelEffectQuestionRepository.cancelEffectQuestion(request.intakeId)
                }
            }
}
