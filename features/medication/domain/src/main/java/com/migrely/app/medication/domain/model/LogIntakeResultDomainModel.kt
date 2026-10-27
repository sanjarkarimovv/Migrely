package com.migrely.app.medication.domain.model

sealed interface LogIntakeResultDomainModel {
    data class Logged(val intake: IntakeDomainModel) : LogIntakeResultDomainModel

    /** More than 5 minutes ahead of now (BRD EC-ATK-03). */
    data object InFuture : LogIntakeResultDomainModel

    data object StorageFull : LogIntakeResultDomainModel

    data object Failed : LogIntakeResultDomainModel
}
