package com.migrely.app.backup.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BackupSummaryUiModel(val attacks: Int, val medications: Int, val dayMarks: Int) :
    Parcelable
