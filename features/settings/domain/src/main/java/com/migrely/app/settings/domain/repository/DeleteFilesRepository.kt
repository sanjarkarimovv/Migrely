package com.migrely.app.settings.domain.repository

import com.migrely.app.settings.domain.model.SettingsWriteResultDomainModel

/** Deletes the automatic backups, then the reports. Files the user exported stay where they were saved. */
interface DeleteFilesRepository {
    fun deleteFiles(): SettingsWriteResultDomainModel
}
