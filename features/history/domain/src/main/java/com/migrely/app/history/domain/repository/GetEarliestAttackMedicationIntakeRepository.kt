package com.migrely.app.history.domain.repository

import com.migrely.app.history.domain.model.EarliestAttackMedicationIntakeResultDomainModel

interface GetEarliestAttackMedicationIntakeRepository {
    /** The first attack-medication intake; preventive medications never count (BRD D99). */
    fun earliestAttackMedicationIntake(): EarliestAttackMedicationIntakeResultDomainModel
}
