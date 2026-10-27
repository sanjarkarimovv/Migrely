package com.migrely.app.backup.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.backup.domain.model.BackupFileDomainModel
import com.migrely.app.backup.domain.model.PhoneRecordsDomainModel
import com.migrely.app.backup.domain.model.ReadBackupResultDomainModel
import com.migrely.app.backup.domain.repository.GetPhoneRecordsRepository
import com.migrely.app.backup.domain.repository.ReadBackupRepository
import com.migrely.app.coroutine.CoroutineContextProvider

/**
 * Checks the picked file before anything changes, and tells whether the phone has records, so the user is asked to
 * replace or add only then (BRD D73, EC-BAK-01–04). The request is the file's content URI.
 */
class ReadBackupUseCase(
    private val readBackupRepository: ReadBackupRepository,
    private val getPhoneRecordsRepository: GetPhoneRecordsRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<String, ReadBackupResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: String): ReadBackupResultDomainModel =
        when (val file = readBackupRepository.readBackup(request)) {
            is BackupFileDomainModel.Readable -> when (getPhoneRecordsRepository.phoneRecords()) {
                PhoneRecordsDomainModel.None -> ReadBackupResultDomainModel.Readable(
                    file.summary,
                    false
                )
                PhoneRecordsDomainModel.Some -> ReadBackupResultDomainModel.Readable(
                    file.summary,
                    true
                )
                PhoneRecordsDomainModel.Failed -> ReadBackupResultDomainModel.Failed
            }

            BackupFileDomainModel.NotABackup -> ReadBackupResultDomainModel.NotABackup
            BackupFileDomainModel.Newer -> ReadBackupResultDomainModel.Newer
            BackupFileDomainModel.Failed -> ReadBackupResultDomainModel.Failed
        }
}
