package com.migrely.app.history.domain.repository

import com.migrely.app.history.domain.model.AttackMedicationIntakesResultDomainModel
import java.time.Instant

interface GetAttackMedicationIntakesRepository {
    /** Intakes of medications for attacks taken from [from] (inclusive) to [to] (exclusive). */
    fun attackMedicationIntakes(
        from: Instant,
        to: Instant
    ): AttackMedicationIntakesResultDomainModel
}
