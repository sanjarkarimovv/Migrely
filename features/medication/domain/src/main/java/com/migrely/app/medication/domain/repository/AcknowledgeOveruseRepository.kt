package com.migrely.app.medication.domain.repository

import com.migrely.app.medication.domain.model.OveruseCategoryDomainModel
import com.migrely.app.medication.domain.model.OveruseLevelDomainModel
import com.migrely.app.medication.domain.model.WriteResultDomainModel
import java.time.Instant

interface AcknowledgeOveruseRepository {
    fun acknowledgeOveruse(
        category: OveruseCategoryDomainModel,
        level: OveruseLevelDomainModel,
        acknowledgedAt: Instant
    ): WriteResultDomainModel
}
