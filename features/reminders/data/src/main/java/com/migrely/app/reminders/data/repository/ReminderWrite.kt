package com.migrely.app.reminders.data.repository

import com.migrely.app.datasource.exception.DataException
import com.migrely.app.datasource.exception.StorageFullDataException
import com.migrely.app.reminders.domain.model.ReminderWriteResultDomainModel

/** A reminders write, with storage failures as values (docs/ARCHITECTURE.md section 7). */
internal inline fun reminderWrite(block: () -> Unit): ReminderWriteResultDomainModel = try {
    block()
    ReminderWriteResultDomainModel.Done
} catch (_: StorageFullDataException) {
    ReminderWriteResultDomainModel.StorageFull
} catch (_: DataException) {
    ReminderWriteResultDomainModel.Failed
}
