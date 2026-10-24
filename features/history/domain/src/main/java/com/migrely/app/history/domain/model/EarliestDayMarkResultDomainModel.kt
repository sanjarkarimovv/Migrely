package com.migrely.app.history.domain.model

import java.time.LocalDate

sealed interface EarliestDayMarkResultDomainModel {
    data class Found(val date: LocalDate) : EarliestDayMarkResultDomainModel

    data object None : EarliestDayMarkResultDomainModel

    data object Failed : EarliestDayMarkResultDomainModel
}
