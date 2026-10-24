package com.migrely.app.home.domain.repository

import com.migrely.app.home.domain.model.DayMarksDomainModel
import java.time.LocalDate

interface GetDayMarksRepository {
    /** The marks of [date]; none when they cannot be read, so home still shows today's attacks. */
    fun dayMarks(date: LocalDate): DayMarksDomainModel
}
