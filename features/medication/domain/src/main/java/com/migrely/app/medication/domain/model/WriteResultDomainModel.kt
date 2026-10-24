package com.migrely.app.medication.domain.model

sealed interface WriteResultDomainModel {
    data object Done : WriteResultDomainModel

    data object StorageFull : WriteResultDomainModel

    data object Failed : WriteResultDomainModel
}
