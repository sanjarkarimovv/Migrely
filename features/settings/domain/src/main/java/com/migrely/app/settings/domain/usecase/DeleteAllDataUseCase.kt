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
 * 1. reminders and notifications, so a reminder that runs meanwhile finds nothing to remind of;
 * 2. the records and the device's state, in one transaction, so a failure there loses nothing;
 * 3. the settings, onboarding's answers included, so the app starts again with onboarding;
 * 4. the automatic backups, then the reports.
 *
 * A step that already ran is safe to run again, so trying again after a failure finishes the job. A daily reminder
 * cancelled before a failure is planned again when the app next starts.
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
            cancelRemindersRepository::cancelReminders,
            deleteRecordsRepository::deleteRecords,
            deletePreferencesRepository::deletePreferences,
            deleteFilesRepository::deleteFiles
        )
        return if (steps.all { step -> step() == SettingsWriteResultDomainModel.Done }) {
            DeleteAllDataResultDomainModel.Deleted
        } else {
            DeleteAllDataResultDomainModel.Failed
        }
    }
}
