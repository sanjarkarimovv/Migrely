package com.migrely.app.reminders.domain.model

import java.time.LocalDate

/** A doctor visit on [date], a day with no time or zone (BRD D168). [id] lets Undo put the same visit back. */
data class DoctorVisitDomainModel(val id: String, val date: LocalDate)
