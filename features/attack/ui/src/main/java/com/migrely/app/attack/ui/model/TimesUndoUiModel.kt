package com.migrely.app.attack.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** What a time change replaced, so "Undo" can put it back (BRD EC-ATK-09). */
@Parcelize
data class TimesUndoUiModel(
    val attackId: String,
    val startEpochMillis: Long,
    val endEpochMillis: Long?,
    val removedPoints: List<IntensityPointUiModel>
) : Parcelable
