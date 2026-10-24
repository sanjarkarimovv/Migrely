package com.migrely.app.medication.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.medication.domain.model.PendingEffectQuestionResultDomainModel
import com.migrely.app.medication.domain.repository.GetCurrentTimeRepository
import com.migrely.app.medication.domain.repository.GetEffectReminderEnabledRepository
import com.migrely.app.medication.domain.repository.GetNotificationsAllowedRepository
import com.migrely.app.medication.domain.repository.GetPendingEffectQuestionRepository
import com.migrely.app.medication.domain.repository.ShowEffectQuestionRepository

/**
 * The planned "Did it help?" run (BRD D67). It asks the question home's card would ask now, so the two always agree,
 * and only while "Ask if a medication helped" is on and notifications are allowed (EC-MED-05 keeps the card).
 */
class RunEffectQuestionUseCase(
    private val getEffectReminderEnabledRepository: GetEffectReminderEnabledRepository,
    private val getNotificationsAllowedRepository: GetNotificationsAllowedRepository,
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val getPendingEffectQuestionRepository: GetPendingEffectQuestionRepository,
    private val showEffectQuestionRepository: ShowEffectQuestionRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, Unit>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit) {
        if (!getEffectReminderEnabledRepository.effectReminderEnabled() ||
            !getNotificationsAllowedRepository.notificationsAllowed()
        ) {
            return
        }
        val now = getCurrentTimeRepository.currentTime().instant
        val result = getPendingEffectQuestionRepository.pendingEffectQuestion(
            takenFrom = now.minus(QUESTION_UNTIL),
            takenTo = now.minus(QUESTION_FROM)
        )
        if (result is PendingEffectQuestionResultDomainModel.Found) {
            showEffectQuestionRepository.showEffectQuestion(
                result.question.intakeId,
                result.question.medicationName
            )
        }
    }
}
