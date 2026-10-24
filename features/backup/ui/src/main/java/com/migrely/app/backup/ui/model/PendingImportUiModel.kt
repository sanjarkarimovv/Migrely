package com.migrely.app.backup.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** A picked backup waiting for the user to confirm, kept across a rotation. */
@Parcelize
data class PendingImportUiModel(
    val uri: String,
    val summary: BackupSummaryUiModel,
    val phoneHasRecords: Boolean
) : Parcelable
