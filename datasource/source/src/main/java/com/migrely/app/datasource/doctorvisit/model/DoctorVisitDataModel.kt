package com.migrely.app.datasource.doctorvisit.model

import java.time.LocalDate

/** A saved doctor visit (BRD D168): a day with no time or zone, like a day mark. */
data class DoctorVisitDataModel(val id: String, val date: LocalDate)
