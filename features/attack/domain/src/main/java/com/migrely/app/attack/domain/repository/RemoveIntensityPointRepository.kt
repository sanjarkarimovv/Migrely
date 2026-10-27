package com.migrely.app.attack.domain.repository

import com.migrely.app.attack.domain.model.IntensityPointDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel

interface RemoveIntensityPointRepository {
    fun removeIntensityPoint(
        attackId: String,
        point: IntensityPointDomainModel
    ): WriteResultDomainModel
}
