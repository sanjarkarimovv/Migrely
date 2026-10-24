package com.migrely.app.backup.data.repository

import com.migrely.app.backup.data.mapper.AttacksCsvMapper
import com.migrely.app.backup.data.mapper.BackupSummaryDomainMapper
import com.migrely.app.backup.domain.model.AutomaticCopyDomainModel
import com.migrely.app.backup.domain.model.BackupFileDomainModel
import com.migrely.app.backup.domain.model.BackupWriteResultDomainModel
import com.migrely.app.backup.domain.model.ImportResultDomainModel
import com.migrely.app.backup.domain.model.PhoneRecordsDomainModel
import com.migrely.app.backup.domain.model.RestoreResultDomainModel
import com.migrely.app.backup.domain.repository.AddBackupRepository
import com.migrely.app.backup.domain.repository.ExportBackupRepository
import com.migrely.app.backup.domain.repository.ExportCsvRepository
import com.migrely.app.backup.domain.repository.GetAutomaticCopyRepository
import com.migrely.app.backup.domain.repository.GetPhoneRecordsRepository
import com.migrely.app.backup.domain.repository.ReadBackupRepository
import com.migrely.app.backup.domain.repository.ReplaceWithBackupRepository
import com.migrely.app.backup.domain.repository.RestoreAutomaticCopyRepository
import com.migrely.app.datasource.backup.datasource.BackupDataSource
import com.migrely.app.datasource.backup.model.BackupRecordsDataModel
import com.migrely.app.datasource.backupfile.datasource.BackupFileDataSource
import com.migrely.app.datasource.backupfile.model.BackupDecodedDataModel
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.document.datasource.DocumentDataSource
import com.migrely.app.datasource.exception.DataException
import com.migrely.app.datasource.exception.StorageFullDataException
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.util.Locale

/** Ten years of entries are a few megabytes; anything far larger is not a Migrely backup, and is not read whole. */
private const val MAXIMUM_BACKUP_BYTES = 16L * 1024 * 1024

/**
 * Backups, exports and the automatic copies (BRD BAK-F1–F3, D71–D73, D98). A spreadsheet in a language that writes
 * decimals with a comma splits columns at semicolons, so the CSV uses one there; Migrely's numbers are whole, so either
 * works.
 */
class BackupRepository(
    private val backupDataSource: BackupDataSource,
    private val backupFileDataSource: BackupFileDataSource,
    private val documentDataSource: DocumentDataSource,
    private val clockDataSource: ClockDataSource,
    private val attacksCsvMapper: AttacksCsvMapper,
    private val backupSummaryDomainMapper: BackupSummaryDomainMapper,
    private val locale: () -> Locale = Locale::getDefault
) : ExportBackupRepository,
    ExportCsvRepository,
    ReadBackupRepository,
    GetPhoneRecordsRepository,
    ReplaceWithBackupRepository,
    AddBackupRepository,
    GetAutomaticCopyRepository,
    RestoreAutomaticCopyRepository {
    override fun exportBackup(uri: String) = write {
        documentDataSource.write(uri, encodedRecords())
    }

    override fun exportCsv(uri: String) = write {
        val separator = if (DecimalFormatSymbols.getInstance(locale()).decimalSeparator ==
            ','
        ) {
            ';'
        } else {
            ','
        }
        documentDataSource.write(uri, attacksCsvMapper.toCsv(backupDataSource.records(), separator))
    }

    override fun readBackup(uri: String): BackupFileDomainModel = try {
        when (val decoded = decode(uri)) {
            is BackupDecodedDataModel.Records ->
                BackupFileDomainModel.Readable(backupSummaryDomainMapper.toDomain(decoded.records))

            BackupDecodedDataModel.NotABackup -> BackupFileDomainModel.NotABackup
            BackupDecodedDataModel.Newer -> BackupFileDomainModel.Newer
        }
    } catch (_: DataException) {
        BackupFileDomainModel.Failed
    }

    override fun phoneRecords(): PhoneRecordsDomainModel = try {
        when (backupDataSource.hasRecords()) {
            true -> PhoneRecordsDomainModel.Some
            false -> PhoneRecordsDomainModel.None
        }
    } catch (_: DataException) {
        PhoneRecordsDomainModel.Failed
    }

    override fun replaceWithBackup(uri: String, saveCopyFirst: Boolean) = import(uri) { records ->
        replace(records, saveCopyFirst)
        ImportResultDomainModel.Replaced(backupSummaryDomainMapper.toDomain(records))
    }

    override fun automaticCopy(): AutomaticCopyDomainModel = try {
        newestAutomaticRecords()?.let { (madeAt, records) ->
            AutomaticCopyDomainModel.Available(madeAt, backupSummaryDomainMapper.toDomain(records))
        } ?: AutomaticCopyDomainModel.None
    } catch (_: DataException) {
        AutomaticCopyDomainModel.None
    }

    override fun restoreAutomaticCopy(saveCopyFirst: Boolean): RestoreResultDomainModel = try {
        // Read before the phone's records are saved, which makes a newer copy.
        newestAutomaticRecords()?.let { (_, records) ->
            replace(records, saveCopyFirst)
            RestoreResultDomainModel.Restored(backupSummaryDomainMapper.toDomain(records))
        } ?: RestoreResultDomainModel.Failed
    } catch (_: StorageFullDataException) {
        RestoreResultDomainModel.StorageFull
    } catch (_: DataException) {
        RestoreResultDomainModel.Failed
    }

    override fun addBackup(uri: String) = import(uri) { records ->
        val added = backupDataSource.add(records)
        ImportResultDomainModel.Added(added.addedAttacks, added.skippedAttacks, added.addedOthers)
    }

    private fun encodedRecords() =
        backupFileDataSource.encode(backupDataSource.records(), clockDataSource.now())

    /**
     * The import's "Replace": what the phone holds is saved as an automatic copy first when asked, then [records] take
     * its place in one transaction. Only records already read as a backup get here, so a failed attempt never pushes
     * an older copy out.
     */
    private fun replace(records: BackupRecordsDataModel, saveCopyFirst: Boolean) {
        if (saveCopyFirst) {
            backupFileDataSource.saveAutomatic(
                encodedRecords(),
                clockDataSource.now()
            )
        }
        backupDataSource.replaceAll(records)
    }

    /** The newest automatic copy with its records, or null when there is none this version can restore. */
    private fun newestAutomaticRecords(): Pair<Instant, BackupRecordsDataModel>? {
        val copy = backupFileDataSource.newestAutomatic() ?: return null
        return when (val decoded = backupFileDataSource.decode(copy.text)) {
            is BackupDecodedDataModel.Records -> copy.madeAt to decoded.records
            // Copies are written by this app, so this means a damaged file; it is not offered.
            BackupDecodedDataModel.NotABackup, BackupDecodedDataModel.Newer -> null
        }
    }

    private fun decode(uri: String) =
        backupFileDataSource.decode(documentDataSource.read(uri, MAXIMUM_BACKUP_BYTES))

    private inline fun import(
        uri: String,
        block: (BackupRecordsDataModel) -> ImportResultDomainModel
    ): ImportResultDomainModel = try {
        when (val decoded = decode(uri)) {
            is BackupDecodedDataModel.Records -> block(decoded.records)
            BackupDecodedDataModel.NotABackup -> ImportResultDomainModel.NotABackup
            BackupDecodedDataModel.Newer -> ImportResultDomainModel.Newer
        }
    } catch (_: StorageFullDataException) {
        ImportResultDomainModel.StorageFull
    } catch (_: DataException) {
        ImportResultDomainModel.Failed
    }

    private inline fun write(block: () -> Unit): BackupWriteResultDomainModel = try {
        block()
        BackupWriteResultDomainModel.Saved
    } catch (_: StorageFullDataException) {
        BackupWriteResultDomainModel.StorageFull
    } catch (_: DataException) {
        BackupWriteResultDomainModel.Failed
    }
}
