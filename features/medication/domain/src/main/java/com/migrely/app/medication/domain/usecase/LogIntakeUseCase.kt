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
import com.migrely.app.medication.domain.repository.GetCurrentTimeRepository
import java.time.Duration

/** Clock tolerance for "now" on another device or a slightly fast clock (BRD EC-ATK-03). */
private val FUTURE_TOLERANCE: Duration = Duration.ofMinutes(5)

/**
 * Logs a medication taken (BRD MED-F3, D34). Without an attack, the intake joins the attack whose time contains it, if
 * any (EC-MED-09). Times more than five minutes ahead are refused.
 */
class LogIntakeUseCase(
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val findAttackAtRepository: FindAttackAtRepository,
    private val addIntakeRepository: AddIntakeRepository,
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
        val attackId = request.attackId ?: when (
            val lookup = findAttackAtRepository.attackAt(request.takenAt)
        ) {
            is AttackLookupResultDomainModel.Found -> lookup.attack.id
            AttackLookupResultDomainModel.None -> null
            AttackLookupResultDomainModel.Failed -> return LogIntakeResultDomainModel.Failed
        }
        return when (
            val added = addIntakeRepository.addIntake(
                medicationId = request.medicationId,
                attackId = attackId,
                takenAt = request.takenAt,
                zone = now.zone,
                dose = request.dose
            )
        ) {
            is AddIntakeResultDomainModel.Added -> LogIntakeResultDomainModel.Logged(
                IntakeDomainModel(
                    id = added.intakeId,
                    medicationId = request.medicationId,
                    medicationName = request.medicationName,
                    dose = request.dose,
                    takenAt = request.takenAt,
                    zone = now.zone,
                    attackId = attackId
                )
            )

            AddIntakeResultDomainModel.StorageFull -> LogIntakeResultDomainModel.StorageFull
            AddIntakeResultDomainModel.Failed -> LogIntakeResultDomainModel.Failed
        }
    }
}
