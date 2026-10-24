package com.migrely.app.reminders.domain.model

import java.time.LocalTime

data class SetDailyReminderRequestDomainModel(val enabled: Boolean, val time: LocalTime)
