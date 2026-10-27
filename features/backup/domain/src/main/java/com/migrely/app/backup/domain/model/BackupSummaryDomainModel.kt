package com.migrely.app.backup.domain.model

/** What a backup holds, shown before it is imported (BRD D73). */
data class BackupSummaryDomainModel(val attacks: Int, val medications: Int, val dayMarks: Int)
