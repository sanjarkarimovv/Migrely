package com.migrely.app.history.domain.repository

import com.migrely.app.history.domain.model.DayMarksResultDomainModel
import java.time.LocalDate

interface GetDayMarksRepository {
    /** The marked dates from [from] to [to], both included. */
    fun dayMarks(from: LocalDate, to: LocalDate): DayMarksResultDomainModel
}
