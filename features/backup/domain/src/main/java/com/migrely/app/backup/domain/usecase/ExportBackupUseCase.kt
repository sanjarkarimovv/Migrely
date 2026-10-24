package com.migrely.app.backup.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.backup.domain.model.BackupWriteResultDomainModel
import com.migrely.app.backup.domain.repository.ExportBackupRepository
import com.migrely.app.coroutine.CoroutineContextProvider

/** "Export a backup (JSON)" to the file the user created, by its content URI (BRD BAK-F1, D71). */
class ExportBackupUseCase(
    private val exportBackupRepository: ExportBackupRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<String, BackupWriteResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: String): BackupWriteResultDomainModel =
        exportBackupRepository.exportBackup(request)
}
