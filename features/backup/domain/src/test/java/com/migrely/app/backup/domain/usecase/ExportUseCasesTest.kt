package com.migrely.app.backup.domain.usecase

import com.migrely.app.backup.domain.model.BackupWriteResultDomainModel
import com.migrely.app.backup.domain.repository.ExportBackupRepository
import com.migrely.app.backup.domain.repository.ExportCsvRepository
import com.migrely.app.coroutine.CoroutineContextProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock

private const val URI = "content://downloads/2"

class ExportUseCasesTest {
    private val exportBackupRepository: ExportBackupRepository = mock()
    private val exportCsvRepository: ExportCsvRepository = mock()

    @Test
    fun `When exporting then the file at the URI is written, with its result`() {
        given(
            exportBackupRepository.exportBackup(URI)
        ).willReturn(BackupWriteResultDomainModel.Saved)
        given(
            exportCsvRepository.exportCsv(URI)
        ).willReturn(BackupWriteResultDomainModel.StorageFull)

        assertEquals(
            BackupWriteResultDomainModel.Saved,
            ExportBackupUseCase(
                exportBackupRepository,
                CoroutineContextProvider.Default
            ).executeInBackground(URI)
        )
        assertEquals(
            BackupWriteResultDomainModel.StorageFull,
            ExportCsvUseCase(
                exportCsvRepository,
                CoroutineContextProvider.Default
            ).executeInBackground(URI)
        )
    }
}
