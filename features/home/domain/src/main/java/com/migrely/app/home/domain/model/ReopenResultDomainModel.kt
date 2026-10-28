package com.migrely.app.home.domain.model

sealed interface ReopenResultDomainModel {
    data object Reopened : ReopenResultDomainModel

    /** In progress again, the attack would overlap another one, or it no longer exists. */
    data object NotPossible : ReopenResultDomainModel

    data object StorageFull : ReopenResultDomainModel

    data object Failed : ReopenResultDomainModel
}
