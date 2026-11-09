package com.migrely.app.datasource.doctorvisit.datasource

import com.migrely.app.datasource.doctorvisit.model.DoctorVisitDataModel
import com.migrely.app.datasource.doctorvisit.model.DoctorVisitsDataModel
import java.time.Instant
import java.time.LocalDate

/**
 * Doctor visits (BRD D168, design D2). At most one is on or after today: saving a next visit replaces every upcoming one
 * in one transaction, which also folds two left by an older state into one. Every call blocks and throws a
 * `DataException` when storage fails.
 */
interface DoctorVisitDataSource {
    /** The next and the last visit, by [today]. */
    fun visits(today: LocalDate): DoctorVisitsDataModel

    /** Makes [date] the only visit on or after [today], under a new ID. */
    fun setNextVisit(date: LocalDate, today: LocalDate)

    /** Takes every visit on or after [today] away and returns the one that was next, or null when there was none. */
    fun removeNextVisit(today: LocalDate): DoctorVisitDataModel?

    /** Puts [visit] back, with its ID, as the only visit on or after [today]. */
    fun restoreNextVisit(visit: DoctorVisitDataModel, today: LocalDate)

    /**
     * Whether an attack, an intake or a pain-free mark falls in the period, BRD D81's question: an attack or an intake
     * at a time from [from] up to [to], or a pain-free mark from [fromDate] to [toDate], both included. Attacks and
     * intakes hidden for Undo do not count.
     */
    fun anythingLoggedBetween(
        from: Instant,
        to: Instant,
        fromDate: LocalDate,
        toDate: LocalDate
    ): Boolean
}
