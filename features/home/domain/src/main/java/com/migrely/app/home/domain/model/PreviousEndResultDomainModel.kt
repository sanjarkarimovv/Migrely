package com.migrely.app.home.domain.model

import java.time.Instant

sealed interface PreviousEndResultDomainModel {
    data class Found(val end: Instant) : PreviousEndResultDomainModel

    data object None : PreviousEndResultDomainModel

    data object Failed : PreviousEndResultDomainModel
}
