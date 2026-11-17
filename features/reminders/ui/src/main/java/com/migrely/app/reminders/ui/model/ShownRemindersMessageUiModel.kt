package com.migrely.app.reminders.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * The message on screen and when it was posted, saved across rotation; the countdown is keyed to it (BRD D182). Two
 * posts of the same text are two values, so a failed retry is a new node and is spoken again (BRD D178 (5)).
 */
@Parcelize
data class ShownRemindersMessageUiModel(
    val message: RemindersMessageUiModel,
    val shownAtEpochMillis: Long
) : Parcelable
