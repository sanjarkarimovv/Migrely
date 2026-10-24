package com.migrely.app.backup.presentation.model

import java.time.Instant

data class AutomaticCopyPresentationModel(
    val madeAt: Instant,
    val summary: BackupSummaryPresentationModel
)
