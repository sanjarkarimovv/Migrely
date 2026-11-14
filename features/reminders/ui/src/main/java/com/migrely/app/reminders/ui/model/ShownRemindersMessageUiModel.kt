package com.migrely.app.reminders.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * The message on screen and when it was posted, saved across rotation so it keeps the time it has left. Two posts of
 * the same text are two values, so a failed retry is a new node and is spoken again (BRD D178 (5)).
 */
@Parcelize
data class ShownRemindersMessageUiModel(
    val message: RemindersMessageUiModel,
    val shownAtEpochMillis: Long
) : Parcelable {
    /** Milliseconds until a message that stays [timeoutMillis] disappears; 0 once that time has passed. */
    fun remainingMillis(nowEpochMillis: Long, timeoutMillis: Long): Long =
        (shownAtEpochMillis + timeoutMillis - nowEpochMillis).coerceAtLeast(0L)
}
