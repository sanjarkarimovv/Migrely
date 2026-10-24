package com.migrely.app.datasource.backupfile.datasource

import com.migrely.app.datasource.backup.model.BackupRecordsDataModel
import com.migrely.app.datasource.backupfile.mapper.BackupFileMapper
import com.migrely.app.datasource.backupfile.model.BackupDecodedDataModel
import com.migrely.app.datasource.backupfile.model.BackupFileHeaderLocalModel
import com.migrely.app.datasource.backupfile.model.BackupFileLocalModel
import com.migrely.app.datasource.backupfile.validator.BackupRecordsValidator
import com.migrely.app.datasource.file.file
import java.io.File
import java.time.DateTimeException
import java.time.Instant
import kotlinx.serialization.json.Json

/** Says what kind of file this is, whatever version wrote it (BRD D71). */
const val BACKUP_FORMAT = "migrely-backup"

/** The database version. Each backup carries the one it was written with (BRD D71). */
const val BACKUP_SCHEMA_VERSION = 6

private const val AUTOMATIC_DIRECTORY = "backups"
private const val AUTOMATIC_PREFIX = "migrely-auto-"
private const val AUTOMATIC_SUFFIX = ".json"
private const val AUTOMATIC_KEPT = 3

/** Written by some editors at the start of a UTF-8 file. An escape here would be stripped by the formatter. */
private val BYTE_ORDER_MARK = Char(0xFEFF)

/** Automatic backups go to [filesDirectory]`/backups`, which Android's backup includes (BRD D74). */
class BackupFileDataSourceImpl(
    private val filesDirectory: File,
    private val backupFileMapper: BackupFileMapper,
    private val backupRecordsValidator: BackupRecordsValidator
) : BackupFileDataSource {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override fun encode(records: BackupRecordsDataModel, exportedAt: Instant): String =
        json.encodeToString(
            BackupFileLocalModel.serializer(),
            backupFileMapper.toLocal(
                records,
                BackupFileMapper.Header(BACKUP_FORMAT, BACKUP_SCHEMA_VERSION, exportedAt)
            )
        )

    @Suppress("SwallowedException")
    override fun decode(text: String): BackupDecodedDataModel = try {
        val content = text.removePrefix(BYTE_ORDER_MARK.toString())
        val header = json.decodeFromString(BackupFileHeaderLocalModel.serializer(), content)
        val schemaVersion = header.schemaVersion
        when {
            header.format != BACKUP_FORMAT || schemaVersion == null || schemaVersion < 1 ->
                BackupDecodedDataModel.NotABackup

            schemaVersion > BACKUP_SCHEMA_VERSION -> BackupDecodedDataModel.Newer

            else -> backupFileMapper.toData(
                json.decodeFromString(BackupFileLocalModel.serializer(), content)
            )
                .takeIf(backupRecordsValidator::isValid)
                ?.let { BackupDecodedDataModel.Records(it, schemaVersion) }
                ?: BackupDecodedDataModel.NotABackup
        }
    } catch (exception: IllegalArgumentException) {
        // kotlinx.serialization's errors are IllegalArgumentExceptions too.
        BackupDecodedDataModel.NotABackup
    } catch (exception: DateTimeException) {
        BackupDecodedDataModel.NotABackup
    }

    override fun saveAutomatic(text: String, madeAt: Instant) = file {
        val directory = File(filesDirectory, AUTOMATIC_DIRECTORY).apply { mkdirs() }
        val name = "$AUTOMATIC_PREFIX${madeAt.toEpochMilli()}$AUTOMATIC_SUFFIX"
        // Written aside first, so a full phone never leaves a half backup behind.
        val partial = File(directory, "$name.partial")
        try {
            partial.writeText(text, Charsets.UTF_8)
            if (!partial.renameTo(File(directory, name))) {
                throw java.io.IOException("Could not save $name")
            }
        } finally {
            partial.delete()
        }
        // The copy just written always stays, even when the clock moved back since the others were made.
        automaticBackups(directory).filter {
            it.name != name
        }.drop(AUTOMATIC_KEPT - 1).forEach(File::delete)
    }

    /** Newest first. */
    private fun automaticBackups(directory: File): List<File> = directory.listFiles().orEmpty()
        .filter { it.name.startsWith(AUTOMATIC_PREFIX) && it.name.endsWith(AUTOMATIC_SUFFIX) }
        .sortedByDescending {
            it.name.removePrefix(AUTOMATIC_PREFIX).removeSuffix(AUTOMATIC_SUFFIX).toLongOrNull()
                ?: 0L
        }
}
