package com.migrely.app.attack.domain.model

sealed interface OverlapLookupResultDomainModel {
    /** Other attacks overlap, oldest first. */
    data class Found(val attacks: List<AttackDetailsDomainModel>) : OverlapLookupResultDomainModel

    data object None : OverlapLookupResultDomainModel

    data object Failed : OverlapLookupResultDomainModel
}
