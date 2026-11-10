package com.migrely.app.datasource.doctorvisit.model

import java.time.LocalDate

/** [next] is the first visit on or after the day asked about; [last] the latest date before it. */
data class DoctorVisitsDataModel(val next: DoctorVisitDataModel?, val last: LocalDate?)
