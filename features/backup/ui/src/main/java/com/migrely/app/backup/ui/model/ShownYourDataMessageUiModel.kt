package com.migrely.app.backup.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * The message on screen and when it was posted, saved across rotation and a restored process so it keeps the time it
 * has left (BRD D178 (2)). Two posts of the same text are two values, so a failed retry is spoken again (D178 (5)).
 */
@Parcelize
data class ShownYourDataMessageUiModel(
    val message: YourDataMessageUiModel,
    val shownAtEpochMillis: Long
) : Parcelable {
    /** Milliseconds until a message that stays [timeoutMillis] disappears; 0 once that time has passed. */
    fun remainingMillis(nowEpochMillis: Long, timeoutMillis: Long): Long =
        (shownAtEpochMillis + timeoutMillis - nowEpochMillis).coerceAtLeast(0L)
}
