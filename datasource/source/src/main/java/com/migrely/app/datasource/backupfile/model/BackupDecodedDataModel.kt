package com.migrely.app.datasource.backupfile.model

import com.migrely.app.datasource.backup.model.BackupRecordsDataModel

/** What a file holds (BRD EC-BAK-01–03). */
sealed interface BackupDecodedDataModel {
    /** A backup this version can import. [schemaVersion] is the one it was written with, this one's or older. */
    data class Records(val records: BackupRecordsDataModel, val schemaVersion: Int) :
        BackupDecodedDataModel

    /** Not a Migrely backup, or one that is broken or edited into something this app cannot store. */
    data object NotABackup : BackupDecodedDataModel

    /** A backup from a newer version of Migrely. */
    data object Newer : BackupDecodedDataModel
}
