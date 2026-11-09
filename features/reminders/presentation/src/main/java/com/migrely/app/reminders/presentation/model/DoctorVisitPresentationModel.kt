package com.migrely.app.reminders.presentation.model

import java.time.LocalDate

/** A removed visit, kept by the screen for Undo. */
data class DoctorVisitPresentationModel(val id: String, val date: LocalDate)
