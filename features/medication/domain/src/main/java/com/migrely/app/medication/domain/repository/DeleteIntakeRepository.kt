package com.migrely.app.medication.domain.repository

import com.migrely.app.medication.domain.model.WriteResultDomainModel

interface DeleteIntakeRepository {
    fun deleteIntake(intakeId: String): WriteResultDomainModel
}
