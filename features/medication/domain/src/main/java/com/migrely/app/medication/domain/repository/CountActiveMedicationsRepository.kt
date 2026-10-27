package com.migrely.app.medication.domain.repository

import com.migrely.app.medication.domain.model.ActiveCountResultDomainModel

interface CountActiveMedicationsRepository {
    fun activeMedicationCount(): ActiveCountResultDomainModel
}
