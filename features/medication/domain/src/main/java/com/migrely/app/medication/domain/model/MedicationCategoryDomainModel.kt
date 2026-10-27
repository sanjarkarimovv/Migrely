package com.migrely.app.medication.domain.model

/** The class of an acute medication (BRD MED-F2). A medication without one is "Not sure" (EC-MED-04). */
enum class MedicationCategoryDomainModel {
    SimplePainkiller,
    Triptan,
    CombinationOrOther
}
