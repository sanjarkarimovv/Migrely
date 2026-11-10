package com.migrely.app.reminders.domain.model

sealed interface SetDoctorVisitResultDomainModel {
    /**
     * The visit is saved and planned. [askForPermission] is true when notifications are off and the 30-day rule allows a
     * request (BRD EC-REM-11, EC-REM-02).
     */
    data class Saved(val askForPermission: Boolean) : SetDoctorVisitResultDomainModel

    /** The date is the next visit already, so nothing is saved again (BRD EC-REM-09). */
    data object Unchanged : SetDoctorVisitResultDomainModel

    data object StorageFull : SetDoctorVisitResultDomainModel

    data object Failed : SetDoctorVisitResultDomainModel
}
