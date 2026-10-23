package com.migrely.app.attack.domain.model

sealed interface WriteResultDomainModel {
    data object Written : WriteResultDomainModel

    data object StorageFull : WriteResultDomainModel

    data object Failed : WriteResultDomainModel
}
