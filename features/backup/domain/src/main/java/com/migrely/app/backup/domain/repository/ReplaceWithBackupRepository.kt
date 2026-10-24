package com.migrely.app.backup.domain.repository

import com.migrely.app.backup.domain.model.ImportResultDomainModel

/** Reads the file at [uri] again and replaces every health record with it, in one transaction. */
interface ReplaceWithBackupRepository {
    fun replaceWithBackup(uri: String): ImportResultDomainModel
}
