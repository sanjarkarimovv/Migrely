package com.migrely.app.datasource.backupfile.datasource

import com.migrely.app.datasource.backup.model.BackupRecordsDataModel
import com.migrely.app.datasource.backupfile.model.AutomaticCopyDataModel
import com.migrely.app.datasource.backupfile.model.BackupDecodedDataModel
import java.time.Instant

/** The backup file format (BRD D71, D73) and the automatic backups kept inside the app. */
interface BackupFileDataSource {
    /** The file's text for [records], stamped with [exportedAt] and this version's schema. */
    fun encode(records: BackupRecordsDataModel, exportedAt: Instant): String

    /** Never throws: a file that cannot be read as a backup is [BackupDecodedDataModel.NotABackup]. */
    fun decode(text: String): BackupDecodedDataModel

    /**
     * Saves [text] as an automatic backup made at [madeAt], keeping the newest three. Throws
     * [com.migrely.app.datasource.exception.StorageFullDataException] when the phone is full and
     * [com.migrely.app.datasource.exception.FileDataException] when it cannot be written.
     */
    fun saveAutomatic(text: String, madeAt: Instant)

    /**
     * The newest automatic backup this version can restore, or null when there is none (BRD D98). A copy that cannot be
     * read, or does not decode as a backup, is passed over for the next one, so a damaged copy never hides older ones.
     */
    fun newestAutomatic(): AutomaticCopyDataModel?

    /** The automatic backup made at [madeAt], or null when it is gone or cannot be restored. */
    fun automatic(madeAt: Instant): AutomaticCopyDataModel?

    /** Deletes every automatic backup (BRD D96). Throws [com.migrely.app.datasource.exception.FileDataException]. */
    fun deleteAll()
}
