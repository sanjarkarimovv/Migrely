package com.migrely.app.history.domain.model

import java.time.LocalDate

data class SetDayMarkRequestDomainModel(
    val date: LocalDate,
    val kind: DayMarkKindDomainModel,
    val marked: Boolean
)
