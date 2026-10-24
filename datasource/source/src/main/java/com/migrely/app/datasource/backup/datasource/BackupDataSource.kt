package com.migrely.app.datasource.backup.datasource

import com.migrely.app.datasource.backup.model.AddedRecordsDataModel
import com.migrely.app.datasource.backup.model.BackupRecordsDataModel

/**
 * Reads and writes every health record at once, for backups (BRD BAK-F1, BAK-F3, D71, D73). Each function runs in one
 * database transaction, blocks, and throws the storage exceptions described on
 * [com.migrely.app.datasource.attack.datasource.AttackDataSource].
 */
interface BackupDataSource {
    fun records(): BackupRecordsDataModel

    /** Whether the phone holds any health record, so an import has to ask how to go about it. */
    fun hasRecords(): Boolean

    /** Deletes every health record and writes [records] in their place. */
    fun replaceAll(records: BackupRecordsDataModel)

    /**
     * Writes the records of [records] the phone does not have yet.
     * - An attack whose ID is present, or which overlaps an attack on the phone, is skipped.
     * - A medication or custom tag whose name is already used stands for the one on the phone.
     * - An intake already present, by ID or by medication and time, is skipped. An intake of an attack that was skipped
     *   for overlapping is kept without the link.
     * - A date already marked keeps its marks.
     */
    fun add(records: BackupRecordsDataModel): AddedRecordsDataModel
}
