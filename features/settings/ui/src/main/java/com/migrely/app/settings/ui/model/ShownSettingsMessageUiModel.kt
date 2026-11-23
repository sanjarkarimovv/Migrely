package com.migrely.app.settings.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * The message on screen and when it was posted, saved across rotation and a restored process so it keeps the time it
 * has left (BRD D178 (2)). Two posts of the same text are two values, so a second failure is spoken again (D178 (5)).
 */
@Parcelize
data class ShownSettingsMessageUiModel(
    val message: SettingsMessageUiModel,
    val shownAtEpochMillis: Long
) : Parcelable
