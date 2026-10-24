package com.migrely.app.reminders.domain.repository

import com.migrely.app.reminders.domain.model.TodayEntriesResultDomainModel
import java.time.Instant
import java.time.LocalDate

interface GetTodayEntriesRepository {
    /** The attacks between [from] and [to], and [date]'s pain-free mark. */
    fun todayEntries(from: Instant, to: Instant, date: LocalDate): TodayEntriesResultDomainModel
}
