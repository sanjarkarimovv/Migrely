package com.migrely.app.medication.domain.model

sealed interface AddIntakeResultDomainModel {
    data class Added(val intakeId: String) : AddIntakeResultDomainModel

    data object StorageFull : AddIntakeResultDomainModel

    data object Failed : AddIntakeResultDomainModel
}
