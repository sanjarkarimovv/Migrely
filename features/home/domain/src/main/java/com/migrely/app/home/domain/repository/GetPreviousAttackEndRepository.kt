package com.migrely.app.home.domain.repository

import com.migrely.app.home.domain.model.PreviousEndResultDomainModel
import java.time.Instant

interface GetPreviousAttackEndRepository {
    /** The latest end among attacks other than [attackId] that ended at or before [before]. */
    fun previousAttackEnd(attackId: String, before: Instant): PreviousEndResultDomainModel
}
