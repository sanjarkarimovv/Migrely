package com.migrely.app.medication.domain.model

sealed interface OveruseStatusResultDomainModel {
    /** The categories with an intake in the last 30 days, in [OveruseCategoryDomainModel]'s order; empty when none. */
    data class Loaded(val rows: List<OveruseStatusRowDomainModel>) : OveruseStatusResultDomainModel

    data object Failed : OveruseStatusResultDomainModel
}
