package com.migrely.app.reminders.domain.repository

import com.migrely.app.reminders.domain.model.AnythingLoggedDomainModel
import java.time.LocalDate
import java.time.ZoneId

interface GetAnythingLoggedRepository {
    /** Whether anything was logged from [from] to [to], both included, as days in [zone]. */
    fun anythingLogged(from: LocalDate, to: LocalDate, zone: ZoneId): AnythingLoggedDomainModel
}
