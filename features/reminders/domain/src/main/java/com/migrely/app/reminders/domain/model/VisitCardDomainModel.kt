package com.migrely.app.reminders.domain.model

import java.time.LocalDate

/** Home's visit card (BRD D168, design D8). */
sealed interface VisitCardDomainModel {
    data object Hidden : VisitCardDomainModel

    data class Shown(val visit: LocalDate) : VisitCardDomainModel
}
