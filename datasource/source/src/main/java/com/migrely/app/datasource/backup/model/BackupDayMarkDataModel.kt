package com.migrely.app.datasource.backup.model

import java.time.Instant
import java.time.LocalDate

data class BackupDayMarkDataModel(
    val date: LocalDate,
    val period: Boolean,
    val painFree: Boolean,
    val updatedAt: Instant
)
