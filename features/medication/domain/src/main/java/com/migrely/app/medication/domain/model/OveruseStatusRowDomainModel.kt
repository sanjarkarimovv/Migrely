package com.migrely.app.medication.domain.model

/**
 * One category's attack-medication days in the last 30 days, against its [limit] (BRD D45, D59). [reached] is the
 * doctor level of home's warning: the limit or more.
 */
data class OveruseStatusRowDomainModel(
    val category: OveruseCategoryDomainModel,
    val days: Int,
    val limit: Int
) {
    val reached: Boolean get() = days >= limit
}
