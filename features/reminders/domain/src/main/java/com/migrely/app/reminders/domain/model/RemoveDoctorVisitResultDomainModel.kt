package com.migrely.app.reminders.domain.model

sealed interface RemoveDoctorVisitResultDomainModel {
    /** [visit] is gone; Undo puts it back (BRD EC-REM-10). */
    data class Removed(val visit: DoctorVisitDomainModel) : RemoveDoctorVisitResultDomainModel

    /** No visit was set, as after a second quick tap on Remove. */
    data object NothingToRemove : RemoveDoctorVisitResultDomainModel

    data object StorageFull : RemoveDoctorVisitResultDomainModel

    data object Failed : RemoveDoctorVisitResultDomainModel
}
