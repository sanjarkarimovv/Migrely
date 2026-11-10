package com.migrely.app.reminders.presentation.model

import java.time.LocalDate

/** Home's visit card (BRD D168, design D8). */
sealed interface VisitCardViewState {
    data object Hidden : VisitCardViewState

    data class Shown(val visit: LocalDate) : VisitCardViewState
}
