package com.migrely.app.medication.domain.model

sealed interface OveruseNoticesResultDomainModel {
    data class Loaded(val notices: List<OveruseNoticeDomainModel>) : OveruseNoticesResultDomainModel

    data object Failed : OveruseNoticesResultDomainModel
}
