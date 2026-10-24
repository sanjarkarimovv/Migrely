package com.migrely.app.medication.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.medication.domain.model.WriteResultDomainModel
import com.migrely.app.medication.domain.repository.CloseEffectQuestionRepository

/**
 * Closes "Did it help?" for an intake without an answer, so it is never asked again (BRD D39). The request is the
 * intake id.
 */
class SkipEffectQuestionUseCase(
    private val closeEffectQuestionRepository: CloseEffectQuestionRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<String, WriteResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: String): WriteResultDomainModel =
        closeEffectQuestionRepository.closeEffectQuestion(request)
}
