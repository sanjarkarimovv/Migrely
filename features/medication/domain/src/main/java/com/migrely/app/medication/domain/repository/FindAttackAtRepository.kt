package com.migrely.app.medication.domain.repository

import com.migrely.app.medication.domain.model.AttackLookupResultDomainModel
import java.time.Instant

interface FindAttackAtRepository {
    /** The attack whose time contains [instant]; an attack in progress runs to now. */
    fun attackAt(instant: Instant): AttackLookupResultDomainModel
}
