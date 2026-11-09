package com.migrely.app.reminders.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * The Reminders screen (BRD D65–D68, D168); [dailyMinuteOfDay] is minutes after midnight, and [nextVisitEpochDay] the
 * next doctor visit, if any.
 */
@Parcelize
data class RemindersUiModel(
    val dailyEnabled: Boolean,
    val dailyMinuteOfDay: Int,
    val effectEnabled: Boolean,
    val notificationsAllowed: Boolean,
    val mayAskPermission: Boolean,
    val nextVisitEpochDay: Long? = null,
    val visitChannelOff: Boolean = false
) : Parcelable
