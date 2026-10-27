package com.migrely.app.settings.domain.repository

import com.migrely.app.settings.domain.model.SettingsWriteResultDomainModel

/** Deletes every health record and the device's own state in one transaction: all of it, or nothing. */
interface DeleteRecordsRepository {
    fun deleteRecords(): SettingsWriteResultDomainModel
}
