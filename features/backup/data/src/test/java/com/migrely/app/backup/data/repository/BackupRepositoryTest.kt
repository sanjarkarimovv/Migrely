package com.migrely.app.backup.data.repository

import com.migrely.app.backup.data.mapper.AttacksCsvMapper
import com.migrely.app.backup.data.mapper.BackupSummaryDomainMapper
import com.migrely.app.backup.domain.model.AutomaticCopyDomainModel
import com.migrely.app.backup.domain.model.BackupFileDomainModel
import com.migrely.app.backup.domain.model.BackupSummaryDomainModel
import com.migrely.app.backup.domain.model.BackupWriteResultDomainModel
import com.migrely.app.backup.domain.model.ImportResultDomainModel
import com.migrely.app.backup.domain.model.PhoneRecordsDomainModel
import com.migrely.app.backup.domain.model.RestoreResultDomainModel
import com.migrely.app.datasource.backup.datasource.BackupDataSource
import com.migrely.app.datasource.backup.model.AddedRecordsDataModel
import com.migrely.app.datasource.backup.model.BackupRecordsDataModel
import com.migrely.app.datasource.backupfile.datasource.BackupFileDataSource
import com.migrely.app.datasource.backupfile.model.AutomaticCopyDataModel
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
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

private const val URI = "content://downloads/1"
private val NOW = Instant.parse("2026-10-25T08:00:00Z")
private val EMPTY =
    BackupRecordsDataModel(emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
private val MADE_AT = Instant.parse("2026-10-24T19:30:00Z")

/** 16 migraines, 2 medications and 5 marked days; only the counts are read. */
private val SIXTEEN = BackupRecordsDataModel(
    attacks = List(16) { mock() },
    customTags = emptyList(),
    medications = List(2) { mock() },
    intakes = emptyList(),
    dayMarks = List(5) { mock() }
)
private val COPY = AutomaticCopyDataModel(MADE_AT, SIXTEEN)

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
    fun `Given records on the phone when replacing then a copy first, only for a backup`() {
        given(documentDataSource.read(eq(URI), any())).willReturn("text")
        given(backupDataSource.records()).willReturn(EMPTY)
        given(backupFileDataSource.encode(EMPTY, NOW)).willReturn("{json}")

        given(backupFileDataSource.decode("text")).willReturn(BackupDecodedDataModel.NotABackup)
        assertEquals(
            ImportResultDomainModel.NotABackup,
            classUnderTest.replaceWithBackup(URI, saveCopyFirst = true)
        )
        verify(backupFileDataSource, never()).saveAutomatic(any(), any())

        given(
            backupFileDataSource.decode("text")
        ).willReturn(BackupDecodedDataModel.Records(EMPTY, 6))
        classUnderTest.replaceWithBackup(URI, saveCopyFirst = true)
        val order = inOrder(backupFileDataSource, backupDataSource)
        order.verify(backupFileDataSource).saveAutomatic("{json}", NOW)
        order.verify(backupDataSource).replaceAll(EMPTY)
    }

    @Test
    fun `Given the copy cannot be saved when replacing then nothing is replaced`() {
        given(documentDataSource.read(eq(URI), any())).willReturn("text")
        given(
            backupFileDataSource.decode("text")
        ).willReturn(BackupDecodedDataModel.Records(EMPTY, 6))
        given(backupDataSource.records()).willReturn(EMPTY)
        given(backupFileDataSource.encode(any(), any())).willReturn("{json}")
        whenever(
            backupFileDataSource.saveAutomatic(any(), any())
        ).doThrow(StorageFullDataException())

        assertEquals(
            ImportResultDomainModel.StorageFull,
            classUnderTest.replaceWithBackup(URI, saveCopyFirst = true)
        )
        verify(backupDataSource, never()).replaceAll(any())
    }

    @Test
    fun `When replacing or adding then the file is read again and written in one call`() {
        given(documentDataSource.read(eq(URI), any())).willReturn("text")
        given(
            backupFileDataSource.decode("text")
        ).willReturn(BackupDecodedDataModel.Records(EMPTY, 5))
        given(
            backupDataSource.add(EMPTY)
        ).willReturn(AddedRecordsDataModel(addedAttacks = 3, skippedAttacks = 1, addedOthers = 2))

        assertEquals(
            ImportResultDomainModel.Replaced(BackupSummaryDomainModel(0, 0, 0)),
            classUnderTest.replaceWithBackup(URI, saveCopyFirst = false)
        )
        assertEquals(ImportResultDomainModel.Added(3, 1, 2), classUnderTest.addBackup(URI))
        verify(backupDataSource).replaceAll(EMPTY)
    }

    /** QA #58: the datasource counts a migraine it brought back from being deleted as added. */
    @Test
    fun `Given a migraine deleted on the phone when adding it back then it counts as added`() {
        val twoMigraines = EMPTY.copy(attacks = List(2) { mock() })
        given(documentDataSource.read(eq(URI), any())).willReturn("text")
        given(
            backupFileDataSource.decode("text")
        ).willReturn(BackupDecodedDataModel.Records(twoMigraines, 6))
        given(
            backupDataSource.add(twoMigraines)
        ).willReturn(AddedRecordsDataModel(addedAttacks = 1, skippedAttacks = 1, addedOthers = 0))

        assertEquals(
            ImportResultDomainModel.Added(addedAttacks = 1, skippedAttacks = 1, addedOthers = 0),
            classUnderTest.addBackup(URI)
        )
        verify(backupDataSource, never()).replaceAll(any())
    }

    @Test
    fun `Given the file changed or the write fails when importing then why, and nothing changed`() {
        given(documentDataSource.read(eq(URI), any())).willReturn("text")

        given(backupFileDataSource.decode("text")).willReturn(BackupDecodedDataModel.Newer)
        assertEquals(
            ImportResultDomainModel.Newer,
            classUnderTest.replaceWithBackup(URI, saveCopyFirst = false)
        )
        given(backupFileDataSource.decode("text")).willReturn(BackupDecodedDataModel.NotABackup)
        assertEquals(ImportResultDomainModel.NotABackup, classUnderTest.addBackup(URI))
        verify(backupDataSource, never()).replaceAll(any())

        given(
            backupFileDataSource.decode("text")
        ).willReturn(BackupDecodedDataModel.Records(EMPTY, 6))
        whenever(backupDataSource.replaceAll(any())).doThrow(StorageFullDataException())
        assertEquals(
            ImportResultDomainModel.StorageFull,
            classUnderTest.replaceWithBackup(URI, saveCopyFirst = false)
        )
        given(backupDataSource.add(any())).willThrow(DatabaseDataException())
        assertEquals(ImportResultDomainModel.Failed, classUnderTest.addBackup(URI))
    }

    @Test
    fun `When asking for the automatic copy then when it was made and what it holds`() {
        given(backupFileDataSource.newestAutomatic()).willReturn(COPY)

        assertEquals(
            AutomaticCopyDomainModel.Available(MADE_AT, BackupSummaryDomainModel(16, 2, 5)),
            classUnderTest.automaticCopy()
        )
    }

    @Test
    fun `Given no copy, or a read failure, then no copy is offered`() {
        assertEquals(AutomaticCopyDomainModel.None, classUnderTest.automaticCopy())

        given(backupFileDataSource.newestAutomatic()).willThrow(FileDataException())
        assertEquals(AutomaticCopyDomainModel.None, classUnderTest.automaticCopy())
    }

    @Test
    fun `When restoring the copy then that copy, after the phone's records are saved first`() {
        given(backupFileDataSource.automatic(MADE_AT)).willReturn(COPY)
        given(backupDataSource.records()).willReturn(EMPTY)
        given(backupFileDataSource.encode(EMPTY, NOW)).willReturn("{json}")

        assertEquals(
            RestoreResultDomainModel.Restored(BackupSummaryDomainModel(16, 2, 5)),
            classUnderTest.restoreAutomaticCopy(MADE_AT, saveCopyFirst = true)
        )
        val order = inOrder(backupFileDataSource, backupDataSource)
        order.verify(backupFileDataSource).automatic(MADE_AT)
        order.verify(backupFileDataSource).saveAutomatic("{json}", NOW)
        order.verify(backupDataSource).replaceAll(SIXTEEN)
        verify(backupFileDataSource, never()).newestAutomatic()
    }

    @Test
    fun `Given an empty phone when restoring then the copy replaces with no copy saved`() {
        given(backupFileDataSource.automatic(MADE_AT)).willReturn(COPY)

        classUnderTest.restoreAutomaticCopy(MADE_AT, saveCopyFirst = false)

        verify(backupFileDataSource, never()).saveAutomatic(any(), any())
        verify(backupDataSource).replaceAll(SIXTEEN)
    }

    @Test
    fun `Given the copy is gone, or the phone fails, then nothing is replaced`() {
        assertEquals(
            RestoreResultDomainModel.Failed,
            classUnderTest.restoreAutomaticCopy(MADE_AT, saveCopyFirst = true)
        )
        verify(backupDataSource, never()).replaceAll(any())

        given(backupFileDataSource.automatic(MADE_AT)).willReturn(COPY)
        given(backupDataSource.records()).willReturn(EMPTY)
        given(backupFileDataSource.encode(any(), any())).willReturn("{json}")
        whenever(
            backupFileDataSource.saveAutomatic(any(), any())
        ).doThrow(StorageFullDataException())
        assertEquals(
            RestoreResultDomainModel.StorageFull,
            classUnderTest.restoreAutomaticCopy(MADE_AT, saveCopyFirst = true)
        )
        verify(backupDataSource, never()).replaceAll(any())

        whenever(backupDataSource.replaceAll(any())).doThrow(DatabaseDataException())
        assertEquals(
            RestoreResultDomainModel.Failed,
            classUnderTest.restoreAutomaticCopy(MADE_AT, saveCopyFirst = false)
        )
    }
}
