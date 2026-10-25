package com.migrely.app.history.domain.repository

import com.migrely.app.history.domain.model.EarliestAttackMedicationIntakeResultDomainModel

interface GetEarliestAttackMedicationIntakeRepository {
    /** The first intake of a medication for attacks; preventive medications never count (BRD D99). */
    fun earliestAttackMedicationIntake(): EarliestAttackMedicationIntakeResultDomainModel
}
