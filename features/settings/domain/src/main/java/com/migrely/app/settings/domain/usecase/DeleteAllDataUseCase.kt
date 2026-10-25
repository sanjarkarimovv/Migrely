package com.migrely.app.settings.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.settings.domain.model.DeleteAllDataResultDomainModel
import com.migrely.app.settings.domain.model.SettingsWriteResultDomainModel
import com.migrely.app.settings.domain.repository.CancelRemindersRepository
import com.migrely.app.settings.domain.repository.DeleteFilesRepository
import com.migrely.app.settings.domain.repository.DeletePreferencesRepository
import com.migrely.app.settings.domain.repository.DeleteRecordsRepository

/**
 * "Delete all data" (BRD EC-STO-03, D96), in this order, stopping at the first step that fails:
 * 1. the records and the device's state, in one transaction, so a failure there has changed nothing, not even a planned
 *    reminder;
 * 2. the automatic backups, then the reports;
 * 3. planned reminders and notifications. A reminder that runs before this finds nothing to remind of: the daily one is
 *    off without its settings, and "Did it help?" finds no intake;
 * 4. the settings last, onboarding's answers included, so the reset that brings onboarding back marks the end.
 *
 * A step that already ran is safe to run again, so trying again after a failure finishes the job.
 */
class DeleteAllDataUseCase(
    private val cancelRemindersRepository: CancelRemindersRepository,
    private val deleteRecordsRepository: DeleteRecordsRepository,
    private val deletePreferencesRepository: DeletePreferencesRepository,
    private val deleteFilesRepository: DeleteFilesRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, DeleteAllDataResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit): DeleteAllDataResultDomainModel {
        val steps = listOf(
            deleteRecordsRepository::deleteRecords,
            deleteFilesRepository::deleteFiles,
            cancelRemindersRepository::cancelReminders,
            deletePreferencesRepository::deletePreferences
        )
        return if (steps.all { step -> step() == SettingsWriteResultDomainModel.Done }) {
            DeleteAllDataResultDomainModel.Deleted
        } else {
            DeleteAllDataResultDomainModel.Failed
        }
    }
}
