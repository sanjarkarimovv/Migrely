package com.migrely.app.medication.domain.repository

import com.migrely.app.medication.domain.model.AttackLookupResultDomainModel
import java.time.Instant

interface FindAttackAtRepository {
    /** An attack whose time overlaps [from, to); an attack in progress runs to now. */
    fun attackDuring(from: Instant, to: Instant): AttackLookupResultDomainModel
}
