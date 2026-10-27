package com.migrely.app.home.domain.repository

import com.migrely.app.home.domain.model.DayMarksResultDomainModel
import java.time.LocalDate

interface GetDayMarksRepository {
    fun dayMarks(date: LocalDate): DayMarksResultDomainModel
}
