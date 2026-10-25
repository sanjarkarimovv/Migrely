package com.migrely.app.backup.ui.model

/** The automatic copy that "Restore the copy" offers, made at [madeAtEpochMillis] (BRD D98). */
data class AutomaticCopyUiModel(val madeAtEpochMillis: Long, val summary: BackupSummaryUiModel)
