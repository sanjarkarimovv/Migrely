package com.migrely.app.history.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** The message on screen and when it appeared, saved across rotation; the countdown is keyed to it (BRD D182). */
@Parcelize
data class ShownHistoryMessageUiModel(
    val message: HistoryMessageUiModel,
    val shownAtEpochMillis: Long
) : Parcelable
