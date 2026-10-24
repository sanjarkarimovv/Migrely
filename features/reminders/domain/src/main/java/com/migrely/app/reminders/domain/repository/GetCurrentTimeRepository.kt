package com.migrely.app.reminders.domain.repository

import com.migrely.app.reminders.domain.model.CurrentTimeDomainModel

interface GetCurrentTimeRepository {
    fun currentTime(): CurrentTimeDomainModel
}
