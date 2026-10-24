package com.migrely.app.backup.domain.model

/** [uri] is the picked file's content URI. */
data class ImportBackupRequestDomainModel(val uri: String, val mode: ImportModeDomainModel)
