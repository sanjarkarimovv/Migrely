package com.migrely.app.backup.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.backup.domain.model.BackupWriteResultDomainModel
import com.migrely.app.backup.domain.model.ImportBackupRequestDomainModel
import com.migrely.app.backup.domain.model.ImportModeDomainModel
import com.migrely.app.backup.domain.model.ImportResultDomainModel
import com.migrely.app.backup.domain.model.PhoneRecordsDomainModel
import com.migrely.app.backup.domain.repository.AddBackupRepository
import com.migrely.app.backup.domain.repository.GetPhoneRecordsRepository
import com.migrely.app.backup.domain.repository.ReplaceWithBackupRepository
import com.migrely.app.backup.domain.repository.SaveAutomaticBackupRepository
import com.migrely.app.coroutine.CoroutineContextProvider

/**
 * Imports the picked backup (BRD BAK-F3, EC-BAK-04, D73). "Replace" first saves what the phone holds as an automatic
 * backup, and changes nothing when that fails. Either way the import is one transaction, so a failure changes nothing.
 */
class ImportBackupUseCase(
    private val getPhoneRecordsRepository: GetPhoneRecordsRepository,
    private val saveAutomaticBackupRepository: SaveAutomaticBackupRepository,
    private val replaceWithBackupRepository: ReplaceWithBackupRepository,
    private val addBackupRepository: AddBackupRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<ImportBackupRequestDomainModel, ImportResultDomainModel>(
    coroutineContextProvider
) {
    override fun executeInBackground(
        request: ImportBackupRequestDomainModel
    ): ImportResultDomainModel = when (request.mode) {
        ImportModeDomainModel.Replace -> replace(request.uri)
        ImportModeDomainModel.Add -> addBackupRepository.addBackup(request.uri)
    }

    private fun replace(uri: String): ImportResultDomainModel {
        val savedFirst = when (getPhoneRecordsRepository.phoneRecords()) {
            PhoneRecordsDomainModel.None -> BackupWriteResultDomainModel.Saved
            PhoneRecordsDomainModel.Some -> saveAutomaticBackupRepository.saveAutomaticBackup()
            PhoneRecordsDomainModel.Failed -> BackupWriteResultDomainModel.Failed
        }
        return when (savedFirst) {
            BackupWriteResultDomainModel.Saved -> replaceWithBackupRepository.replaceWithBackup(uri)
            BackupWriteResultDomainModel.StorageFull -> ImportResultDomainModel.StorageFull
            BackupWriteResultDomainModel.Failed -> ImportResultDomainModel.Failed
        }
    }
}
