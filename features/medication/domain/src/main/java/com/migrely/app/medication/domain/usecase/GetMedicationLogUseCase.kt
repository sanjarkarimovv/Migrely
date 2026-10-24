package com.migrely.app.medication.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.medication.domain.model.AttackLookupResultDomainModel
import com.migrely.app.medication.domain.model.MedicationKindDomainModel
import com.migrely.app.medication.domain.model.MedicationLogDomainModel
import com.migrely.app.medication.domain.model.MedicationLogResultDomainModel
import com.migrely.app.medication.domain.model.MedicationsResultDomainModel
import com.migrely.app.medication.domain.repository.GetActiveMedicationsRepository
import com.migrely.app.medication.domain.repository.GetAttackRepository
import com.migrely.app.medication.domain.repository.GetCurrentTimeRepository
import java.time.temporal.ChronoUnit

/**
 * What "Medication taken" starts with (BRD D34). The request is the attack it was opened for, or null. The time is now,
 * or the start of that attack once it has ended; an attack that no longer exists is ignored. Acute medications come
 * first, each group keeping the most-recently-used order.
 */
class GetMedicationLogUseCase(
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val getAttackRepository: GetAttackRepository,
    private val getActiveMedicationsRepository: GetActiveMedicationsRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<String?, MedicationLogResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: String?): MedicationLogResultDomainModel {
        val now = getCurrentTimeRepository.currentTime()
        val attack = when (val lookup = request?.let(getAttackRepository::attack)) {
            is AttackLookupResultDomainModel.Found -> lookup.attack
            AttackLookupResultDomainModel.None, null -> null
            AttackLookupResultDomainModel.Failed -> return MedicationLogResultDomainModel.Failed
        }
        val medications = when (val result = getActiveMedicationsRepository.activeMedications()) {
            is MedicationsResultDomainModel.Loaded -> result.medications
            MedicationsResultDomainModel.Failed -> return MedicationLogResultDomainModel.Failed
        }
        val defaultTime = if (attack?.end != null) attack.start else now.instant
        return MedicationLogResultDomainModel.Loaded(
            MedicationLogDomainModel(
                defaultTime = defaultTime.truncatedTo(ChronoUnit.MINUTES),
                zone = now.zone,
                attackId = attack?.id,
                medications = medications.sortedBy { it.kind != MedicationKindDomainModel.Acute }
            )
        )
    }
}
