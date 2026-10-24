package com.migrely.app.medication.domain.repository

import com.migrely.app.medication.domain.model.AcuteIntakesResultDomainModel
import java.time.Instant

interface GetAcuteIntakesRepository {
    fun acuteIntakesTakenBetween(from: Instant, to: Instant): AcuteIntakesResultDomainModel
}
