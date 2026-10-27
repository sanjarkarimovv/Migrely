package com.migrely.app.medication.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.medication.domain.model.AttackLookupResultDomainModel
import com.migrely.app.medication.domain.model.PendingEffectQuestionResultDomainModel
import com.migrely.app.medication.domain.repository.FindAttackAtRepository
import com.migrely.app.medication.domain.repository.GetCurrentTimeRepository
import com.migrely.app.medication.domain.repository.GetEffectReminderEnabledRepository
import com.migrely.app.medication.domain.repository.GetNotificationsAllowedRepository
import com.migrely.app.medication.domain.repository.GetPendingEffectQuestionOfRepository
import com.migrely.app.medication.domain.repository.ShowEffectQuestionRepository
import java.time.Duration

/**
 * The planned "Did it help?" run for one intake (BRD D67): it asks only while that intake's question is still open, so an
 * answered, skipped, undone or preventive intake asks nothing, and only while "Ask if a medication helped" is on and
 * notifications are allowed (EC-MED-05 keeps the card). Nothing is sent while an attack is in progress (D69).
 */
private val ONE_MINUTE: Duration = Duration.ofMinutes(1)

class RunEffectQuestionUseCase(
    private val getEffectReminderEnabledRepository: GetEffectReminderEnabledRepository,
    private val getNotificationsAllowedRepository: GetNotificationsAllowedRepository,
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val findAttackAtRepository: FindAttackAtRepository,
    private val getPendingEffectQuestionOfRepository: GetPendingEffectQuestionOfRepository,
    private val showEffectQuestionRepository: ShowEffectQuestionRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<String, Unit>(coroutineContextProvider) {
    override fun executeInBackground(request: String) {
        if (!getEffectReminderEnabledRepository.effectReminderEnabled() ||
            !getNotificationsAllowedRepository.notificationsAllowed()
        ) {
            return
        }
        val now = getCurrentTimeRepository.currentTime().instant
        // No notification during an attack, when light and sound hurt; home's card asks once it ends (BRD D69).
        if (findAttackAtRepository.attackDuring(
                now,
                now.plus(ONE_MINUTE)
            ) is AttackLookupResultDomainModel.Found
        ) {
            return
        }
        val result = getPendingEffectQuestionOfRepository.pendingEffectQuestionOf(
            intakeId = request,
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
