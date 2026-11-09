package com.migrely.app.backup.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.backup.domain.model.ImportBackupRequestDomainModel
import com.migrely.app.backup.domain.model.ImportModeDomainModel
import com.migrely.app.backup.domain.model.ImportResultDomainModel
import com.migrely.app.backup.domain.model.PhoneRecordsDomainModel
import com.migrely.app.backup.domain.repository.AddBackupRepository
import com.migrely.app.backup.domain.repository.GetPhoneRecordsRepository
import com.migrely.app.backup.domain.repository.PlanRemindersAgainRepository
import com.migrely.app.backup.domain.repository.ReplaceWithBackupRepository
import com.migrely.app.coroutine.CoroutineContextProvider

/**
 * Imports the picked backup (BRD BAK-F3, EC-BAK-04, D73). "Replace" over records on the phone saves them as an automatic
 * backup first, and changes nothing when that fails. Either way the import is one transaction, so a failure changes
 * nothing. After an import the reminders are planned again, since a doctor visit may have come or gone (design D7).
 */
class ImportBackupUseCase(
    private val getPhoneRecordsRepository: GetPhoneRecordsRepository,
    private val replaceWithBackupRepository: ReplaceWithBackupRepository,
    private val addBackupRepository: AddBackupRepository,
    private val planRemindersAgainRepository: PlanRemindersAgainRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<ImportBackupRequestDomainModel, ImportResultDomainModel>(
    coroutineContextProvider
) {
    override fun executeInBackground(
        request: ImportBackupRequestDomainModel
    ): ImportResultDomainModel = import(request).also { result ->
        if (result is ImportResultDomainModel.Replaced || result is ImportResultDomainModel.Added) {
            planRemindersAgainRepository.planRemindersAgain()
        }
    }

    private fun import(request: ImportBackupRequestDomainModel): ImportResultDomainModel =
        when (request.mode) {
            ImportModeDomainModel.Replace -> when (getPhoneRecordsRepository.phoneRecords()) {
                PhoneRecordsDomainModel.None ->
                    replaceWithBackupRepository.replaceWithBackup(
                        request.uri,
                        saveCopyFirst = false
                    )
                PhoneRecordsDomainModel.Some ->
                    replaceWithBackupRepository.replaceWithBackup(request.uri, saveCopyFirst = true)
                PhoneRecordsDomainModel.Failed -> ImportResultDomainModel.Failed
            }
            ImportModeDomainModel.Add -> addBackupRepository.addBackup(request.uri)
        }
}
