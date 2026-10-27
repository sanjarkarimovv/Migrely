package com.migrely.app.home.domain.model

sealed interface ReopenResultDomainModel {
    data object Reopened : ReopenResultDomainModel

    data object AnotherInProgress : ReopenResultDomainModel

    data object StorageFull : ReopenResultDomainModel

    data object Failed : ReopenResultDomainModel
}
