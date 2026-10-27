package com.migrely.app.attack.domain.repository

import com.migrely.app.attack.domain.model.WriteResultDomainModel

interface RemoveIntakeRepository {
    fun removeIntake(intakeId: String): WriteResultDomainModel
}
