package com.migrely.app.home.domain.model

sealed interface WriteResultDomainModel {
    data object Written : WriteResultDomainModel

    data object StorageFull : WriteResultDomainModel

    data object Failed : WriteResultDomainModel
}
