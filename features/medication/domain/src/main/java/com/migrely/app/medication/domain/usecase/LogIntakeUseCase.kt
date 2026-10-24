package com.migrely.app.medication.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.medication.domain.model.AddIntakeResultDomainModel
import com.migrely.app.medication.domain.model.AttackLookupResultDomainModel
import com.migrely.app.medication.domain.model.IntakeDomainModel
import com.migrely.app.medication.domain.model.LogIntakeRequestDomainModel
import com.migrely.app.medication.domain.model.LogIntakeResultDomainModel
import com.migrely.app.medication.domain.repository.AddIntakeRepository
import com.migrely.app.medication.domain.repository.FindAttackAtRepository
import com.migrely.app.medication.domain.repository.GetAttackRepository
import com.migrely.app.medication.domain.repository.GetCurrentTimeRepository
import com.migrely.app.medication.domain.repository.ScheduleEffectQuestionRepository
import java.time.Duration

/** Clock tolerance for "now" on another device or a slightly fast clock (BRD EC-ATK-03). */
private val FUTURE_TOLERANCE: Duration = Duration.ofMinutes(5)

/** Times are picked to the minute, so an attack that starts within the picked minute contains it. */
private val PICKED_MINUTE: Duration = Duration.ofMinutes(1)

/**
 * Logs a medication taken (BRD MED-F3, D34). Without an attack, the intake joins the attack whose time contains its
 * minute, if any (EC-MED-09). Times more than five minutes ahead are refused. The intake keeps the zone its time was
 * picked in: the attack's own zone when logged for an attack, otherwise the device's (EC-TIM-02).
 */
class LogIntakeUseCase(
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val getAttackRepository: GetAttackRepository,
    private val findAttackAtRepository: FindAttackAtRepository,
    private val addIntakeRepository: AddIntakeRepository,
    private val scheduleEffectQuestionRepository: ScheduleEffectQuestionRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<LogIntakeRequestDomainModel, LogIntakeResultDomainModel>(
    coroutineContextProvider
) {
    override fun executeInBackground(
        request: LogIntakeRequestDomainModel
    ): LogIntakeResultDomainModel {
        val now = getCurrentTimeRepository.currentTime()
        if (request.takenAt.isAfter(now.instant.plus(FUTURE_TOLERANCE))) {
            return LogIntakeResultDomainModel.InFuture
        }
        val attack = when (
            val lookup = request.attackId?.let(getAttackRepository::attack)
                ?: findAttackAtRepository.attackDuring(
                    request.takenAt,
                    request.takenAt.plus(PICKED_MINUTE)
                )
        ) {
            is AttackLookupResultDomainModel.Found -> lookup.attack
            AttackLookupResultDomainModel.None -> null
            AttackLookupResultDomainModel.Failed -> return LogIntakeResultDomainModel.Failed
        }
        val attackId = request.attackId ?: attack?.id
        val zone = request.attackId?.let { attack?.zone } ?: now.zone
        return when (
            val added = addIntakeRepository.addIntake(
                medicationId = request.medicationId,
                attackId = attackId,
                takenAt = request.takenAt,
                zone = zone,
                dose = request.dose
            )
        ) {
            is AddIntakeResultDomainModel.Added -> {
                // The run itself checks that the question is still open, so a preventive intake asks nothing (D67).
                scheduleEffectQuestionRepository.scheduleEffectQuestion(
                    added.intakeId,
                    request.takenAt.plus(QUESTION_FROM)
                )
                LogIntakeResultDomainModel.Logged(
                    IntakeDomainModel(
                        id = added.intakeId,
                        medicationId = request.medicationId,
                        medicationName = request.medicationName,
                        dose = request.dose,
                        takenAt = request.takenAt,
                        zone = zone,
                        attackId = attackId
                    )
                )
            }

            AddIntakeResultDomainModel.StorageFull -> LogIntakeResultDomainModel.StorageFull
            AddIntakeResultDomainModel.Failed -> LogIntakeResultDomainModel.Failed
        }
    }
}
