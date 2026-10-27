package com.migrely.app.home.domain.model

sealed interface RecordIntensityResultDomainModel {
    data object Recorded : RecordIntensityResultDomainModel

    data object NotInProgress : RecordIntensityResultDomainModel

    data object StorageFull : RecordIntensityResultDomainModel

    data object Failed : RecordIntensityResultDomainModel
}
