package com.migrely.app.reminders.domain.model

import java.time.LocalDate

sealed interface DoctorVisitsResultDomainModel {
    /** [next] is the first visit on or after today, [last] the latest before today (BRD EC-REM-15). */
    data class Loaded(val next: DoctorVisitDomainModel?, val last: LocalDate?) :
        DoctorVisitsResultDomainModel

    data object Failed : DoctorVisitsResultDomainModel
}
