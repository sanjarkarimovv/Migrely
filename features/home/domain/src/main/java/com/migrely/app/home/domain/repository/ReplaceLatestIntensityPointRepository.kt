package com.migrely.app.home.domain.repository

import com.migrely.app.home.domain.model.IntensityPointDomainModel
import com.migrely.app.home.domain.model.WriteResultDomainModel

interface ReplaceLatestIntensityPointRepository {
    fun replaceLatestIntensityPoint(
        attackId: String,
        point: IntensityPointDomainModel
    ): WriteResultDomainModel
}
