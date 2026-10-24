package com.migrely.app.backup.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.backup.domain.model.BackupWriteResultDomainModel
import com.migrely.app.backup.domain.repository.ExportCsvRepository
import com.migrely.app.coroutine.CoroutineContextProvider

/** "Export for Excel (CSV)" to the file the user created, by its content URI (BRD BAK-F2, D72). */
class ExportCsvUseCase(
    private val exportCsvRepository: ExportCsvRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<String, BackupWriteResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: String): BackupWriteResultDomainModel =
        exportCsvRepository.exportCsv(request)
}
