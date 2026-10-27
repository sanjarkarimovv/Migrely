package com.migrely.app.home.domain.model

import java.time.Instant

sealed interface MoveStartResultDomainModel {
    data class Moved(val newStart: Instant) : MoveStartResultDomainModel

    data class OverlapsPrevious(val previousEnd: Instant) : MoveStartResultDomainModel

    data object NotInProgress : MoveStartResultDomainModel

    data object StorageFull : MoveStartResultDomainModel

    data object Failed : MoveStartResultDomainModel
}
