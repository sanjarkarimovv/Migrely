package com.migrely.app.datasource.medication.model

/** What an overuse warning counts: one category, or all acute medications together (ICHD-3 8.2.6, BRD D45). */
enum class OveruseCategoryDataModel {
    SimplePainkiller,
    Triptan,
    CombinationOrOther,
    Multiple
}
