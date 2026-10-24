package com.migrely.app.backup.domain.repository

import com.migrely.app.backup.domain.model.ImportResultDomainModel

/**
 * Reads the file at [uri] again and replaces every health record with it, in one transaction. With [saveCopyFirst],
 * what the phone holds is saved as an automatic backup first, and only once the file has been read as a backup; when
 * that copy cannot be saved, nothing is replaced (BRD EC-BAK-04, D73).
 */
interface ReplaceWithBackupRepository {
    fun replaceWithBackup(uri: String, saveCopyFirst: Boolean): ImportResultDomainModel
}
