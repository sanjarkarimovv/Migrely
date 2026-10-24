package com.migrely.app.backup.data.repository

import com.migrely.app.backup.data.mapper.AttacksCsvMapper
import com.migrely.app.backup.data.mapper.BackupSummaryDomainMapper
import com.migrely.app.backup.domain.model.BackupFileDomainModel
import com.migrely.app.backup.domain.model.BackupSummaryDomainModel
import com.migrely.app.backup.domain.model.BackupWriteResultDomainModel
import com.migrely.app.backup.domain.model.ImportResultDomainModel
import com.migrely.app.backup.domain.model.PhoneRecordsDomainModel
import com.migrely.app.datasource.backup.datasource.BackupDataSource
import com.migrely.app.datasource.backup.model.AddedRecordsDataModel
import com.migrely.app.datasource.backup.model.BackupRecordsDataModel
import com.migrely.app.datasource.backupfile.datasource.BackupFileDataSource
import com.migrely.app.datasource.backupfile.model.BackupDecodedDataModel
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.document.datasource.DocumentDataSource
import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.datasource.exception.FileDataException
import com.migrely.app.datasource.exception.StorageFullDataException
import java.time.Instant
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

private const val URI = "content://downloads/1"
private val NOW = Instant.parse("2026-10-25T08:00:00Z")
private val EMPTY =
    BackupRecordsDataModel(emptyList(), emptyList(), emptyList(), emptyList(), emptyList())

class BackupRepositoryTest {
    private val backupDataSource: BackupDataSource = mock()
    private val backupFileDataSource: BackupFileDataSource = mock()
    private val documentDataSource: DocumentDataSource = mock()
    private val clockDataSource: ClockDataSource = mock {
        on { now() } doReturn NOW
    }
    private var locale: Locale = Locale.US
    private val classUnderTest = BackupRepository(
        backupDataSource,
        backupFileDataSource,
        documentDataSource,
        clockDataSource,
        AttacksCsvMapper(),
        BackupSummaryDomainMapper()
    ) { locale }

    @Test
    fun `When exporting a backup then the phone's records are written to the file`() {
        given(backupDataSource.records()).willReturn(EMPTY)
        given(backupFileDataSource.encode(EMPTY, NOW)).willReturn("{json}")

        assertEquals(BackupWriteResultDomainModel.Saved, classUnderTest.exportBackup(URI))
        verify(documentDataSource).write(URI, "{json}")
    }

    @Test
    fun `Given writing fails when exporting then storage full or failed`() {
        given(backupDataSource.records()).willReturn(EMPTY)
        given(backupFileDataSource.encode(any(), any())).willReturn("{json}")

        whenever(documentDataSource.write(any(), any())).doThrow(StorageFullDataException())
        assertEquals(BackupWriteResultDomainModel.StorageFull, classUnderTest.exportBackup(URI))

        whenever(documentDataSource.write(any(), any())).doThrow(FileDataException())
        assertEquals(BackupWriteResultDomainModel.Failed, classUnderTest.exportCsv(URI))

        given(backupDataSource.records()).willThrow(DatabaseDataException())
        assertEquals(BackupWriteResultDomainModel.Failed, classUnderTest.exportBackup(URI))
    }

    @Test
    fun `When exporting for Excel then commas, or semicolons where decimals take a comma`() {
        given(backupDataSource.records()).willReturn(EMPTY)
        val written = argumentCaptor<String>()

        classUnderTest.exportCsv(URI)
        locale = Locale.forLanguageTag("uz-UZ")
        classUnderTest.exportCsv(URI)
        locale = Locale.forLanguageTag("ru-RU")
        classUnderTest.exportCsv(URI)

        verify(documentDataSource, times(3)).write(eq(URI), written.capture())
        assertEquals(listOf(true, false, false), written.allValues.map { it.contains("Start,End") })
        assertEquals(listOf(false, true, true), written.allValues.map { it.contains("Start;End") })
    }

    @Test
    fun `When reading a file then what it holds or why not, without changing anything`() {
        given(documentDataSource.read(eq(URI), any())).willReturn("text")

        given(
            backupFileDataSource.decode("text")
        ).willReturn(BackupDecodedDataModel.Records(EMPTY, 6))
        assertEquals(
            BackupFileDomainModel.Readable(BackupSummaryDomainModel(0, 0, 0)),
            classUnderTest.readBackup(URI)
        )

        given(backupFileDataSource.decode("text")).willReturn(BackupDecodedDataModel.NotABackup)
        assertEquals(BackupFileDomainModel.NotABackup, classUnderTest.readBackup(URI))

        given(backupFileDataSource.decode("text")).willReturn(BackupDecodedDataModel.Newer)
        assertEquals(BackupFileDomainModel.Newer, classUnderTest.readBackup(URI))

        given(documentDataSource.read(eq(URI), any())).willThrow(FileDataException())
        assertEquals(BackupFileDomainModel.Failed, classUnderTest.readBackup(URI))

        verify(backupDataSource, never()).replaceAll(any())
        verify(backupDataSource, never()).add(any())
    }

    @Test
    fun `When reading a file then at most 16 MB of it`() {
        given(documentDataSource.read(any(), any())).willReturn("text")
        given(backupFileDataSource.decode(any())).willReturn(BackupDecodedDataModel.NotABackup)

        classUnderTest.readBackup(URI)

        verify(documentDataSource).read(URI, 16L * 1024 * 1024)
    }

    @Test
    fun `When asking about the phone then some, none or failed`() {
        given(backupDataSource.hasRecords()).willReturn(true)
        assertEquals(PhoneRecordsDomainModel.Some, classUnderTest.phoneRecords())

        given(backupDataSource.hasRecords()).willReturn(false)
        assertEquals(PhoneRecordsDomainModel.None, classUnderTest.phoneRecords())

        given(backupDataSource.hasRecords()).willThrow(DatabaseDataException())
        assertEquals(PhoneRecordsDomainModel.Failed, classUnderTest.phoneRecords())
    }

    @Test
    fun `When saving the automatic backup then the phone's records, stamped now`() {
        given(backupDataSource.records()).willReturn(EMPTY)
        given(backupFileDataSource.encode(EMPTY, NOW)).willReturn("{json}")

        assertEquals(BackupWriteResultDomainModel.Saved, classUnderTest.saveAutomaticBackup())
        verify(backupFileDataSource).saveAutomatic("{json}", NOW)

        whenever(
            backupFileDataSource.saveAutomatic(any(), any())
        ).doThrow(StorageFullDataException())
        assertEquals(BackupWriteResultDomainModel.StorageFull, classUnderTest.saveAutomaticBackup())
    }

    @Test
    fun `When replacing or adding then the file is read again and written in one call`() {
        given(documentDataSource.read(eq(URI), any())).willReturn("text")
        given(
            backupFileDataSource.decode("text")
        ).willReturn(BackupDecodedDataModel.Records(EMPTY, 5))
        given(
            backupDataSource.add(EMPTY)
        ).willReturn(AddedRecordsDataModel(addedAttacks = 3, skippedAttacks = 1))

        assertEquals(
            ImportResultDomainModel.Replaced(BackupSummaryDomainModel(0, 0, 0)),
            classUnderTest.replaceWithBackup(URI)
        )
        assertEquals(ImportResultDomainModel.Added(3, 1), classUnderTest.addBackup(URI))
        verify(backupDataSource).replaceAll(EMPTY)
    }

    @Test
    fun `Given the file changed or the write fails when importing then why, and nothing changed`() {
        given(documentDataSource.read(eq(URI), any())).willReturn("text")

        given(backupFileDataSource.decode("text")).willReturn(BackupDecodedDataModel.Newer)
        assertEquals(ImportResultDomainModel.Newer, classUnderTest.replaceWithBackup(URI))
        given(backupFileDataSource.decode("text")).willReturn(BackupDecodedDataModel.NotABackup)
        assertEquals(ImportResultDomainModel.NotABackup, classUnderTest.addBackup(URI))
        verify(backupDataSource, never()).replaceAll(any())

        given(
            backupFileDataSource.decode("text")
        ).willReturn(BackupDecodedDataModel.Records(EMPTY, 6))
        whenever(backupDataSource.replaceAll(any())).doThrow(StorageFullDataException())
        assertEquals(ImportResultDomainModel.StorageFull, classUnderTest.replaceWithBackup(URI))
        given(backupDataSource.add(any())).willThrow(DatabaseDataException())
        assertEquals(ImportResultDomainModel.Failed, classUnderTest.addBackup(URI))
    }
}
