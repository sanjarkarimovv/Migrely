package com.migrely.app.reminders.domain.model

sealed interface ReminderWriteResultDomainModel {
    data object Done : ReminderWriteResultDomainModel

    data object StorageFull : ReminderWriteResultDomainModel

    data object Failed : ReminderWriteResultDomainModel
}
