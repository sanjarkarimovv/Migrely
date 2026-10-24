package com.migrely.app.reminders.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** The Reminders screen (BRD D65–D68); [dailyMinuteOfDay] is minutes after midnight. */
@Parcelize
data class RemindersUiModel(
    val dailyEnabled: Boolean,
    val dailyMinuteOfDay: Int,
    val effectEnabled: Boolean,
    val notificationsAllowed: Boolean,
    val mayAskPermission: Boolean
) : Parcelable
