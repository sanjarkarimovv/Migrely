package com.migrely.app.history.domain.model

import java.time.LocalDate

/** The marks stored for a date (BRD D47). */
data class DayMarkDomainModel(val date: LocalDate, val period: Boolean, val painFree: Boolean)
