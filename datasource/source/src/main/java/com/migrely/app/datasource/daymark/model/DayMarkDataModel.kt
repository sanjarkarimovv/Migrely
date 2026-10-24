package com.migrely.app.datasource.daymark.model

import java.time.LocalDate

/** The marks of one calendar date, without a zone (BRD D47, EC-DAY-04). */
data class DayMarkDataModel(val date: LocalDate, val period: Boolean, val painFree: Boolean)
