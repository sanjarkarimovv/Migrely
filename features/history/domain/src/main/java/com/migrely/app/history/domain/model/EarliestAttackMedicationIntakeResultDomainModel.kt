package com.migrely.app.history.domain.model

import java.time.Instant
import java.time.ZoneId

sealed interface EarliestAttackMedicationIntakeResultDomainModel {
    /** The first attack-medication intake was taken at [takenAt] in [zone] (BRD D99). */
    data class Found(val takenAt: Instant, val zone: ZoneId) :
        EarliestAttackMedicationIntakeResultDomainModel

    data object None : EarliestAttackMedicationIntakeResultDomainModel

    data object Failed : EarliestAttackMedicationIntakeResultDomainModel
}
