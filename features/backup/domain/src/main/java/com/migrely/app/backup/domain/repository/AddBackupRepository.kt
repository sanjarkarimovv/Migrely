package com.migrely.app.backup.domain.repository

import com.migrely.app.backup.domain.model.ImportResultDomainModel

/** Reads the file at [uri] again and adds what the phone does not have yet, in one transaction. */
interface AddBackupRepository {
    fun addBackup(uri: String): ImportResultDomainModel
}
