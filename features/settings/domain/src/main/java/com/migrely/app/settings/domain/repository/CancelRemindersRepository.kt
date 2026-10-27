package com.migrely.app.settings.domain.repository

import com.migrely.app.settings.domain.model.SettingsWriteResultDomainModel

/** Cancels every planned reminder and removes every notification. */
interface CancelRemindersRepository {
    fun cancelReminders(): SettingsWriteResultDomainModel
}
