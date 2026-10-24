package com.migrely.app.attack.domain.repository

import com.migrely.app.attack.domain.model.FunctionalImpactDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel

interface SetFunctionalImpactRepository {
    fun setFunctionalImpact(
        attackId: String,
        impact: FunctionalImpactDomainModel?
    ): WriteResultDomainModel
}
