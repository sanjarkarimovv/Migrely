package com.migrely.app.medication.domain.model

/**
 * What an overuse warning counts (BRD D45), with its threshold in days within 30: one category, or all acute
 * medications together when two or more categories were used (ICHD-3 8.2.6).
 */
enum class OveruseCategoryDomainModel(val thresholdDays: Int) {
    SimplePainkiller(15),
    Triptan(10),
    CombinationOrOther(10),
    Multiple(10)
}
