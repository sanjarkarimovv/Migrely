package com.migrely.app.reminders.domain.repository

import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel
import java.time.Instant

interface SetPermissionAskedRepository {
    fun setPermissionAsked(at: Instant): ReminderWriteResultDomainModel
}
