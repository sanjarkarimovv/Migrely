package com.migrely.app.attack.domain.model

sealed interface MaximumRecordedResultDomainModel {
    data object Recorded : MaximumRecordedResultDomainModel

    data object NotRecorded : MaximumRecordedResultDomainModel

    data object Failed : MaximumRecordedResultDomainModel
}
