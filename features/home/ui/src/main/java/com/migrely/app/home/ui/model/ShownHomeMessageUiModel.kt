package com.migrely.app.home.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** The message on screen and when it appeared, saved across rotation so it keeps the time it has left. */
@Parcelize
data class ShownHomeMessageUiModel(val message: HomeMessageUiModel, val shownAtEpochMillis: Long) :
    Parcelable {
    /** Milliseconds until a message that stays [timeoutMillis] disappears; 0 once that time has passed. */
    fun remainingMillis(nowEpochMillis: Long, timeoutMillis: Long): Long =
        (shownAtEpochMillis + timeoutMillis - nowEpochMillis).coerceAtLeast(0L)
}
