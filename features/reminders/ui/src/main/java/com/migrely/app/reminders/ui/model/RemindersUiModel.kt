package com.migrely.app.reminders.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * The Reminders screen (BRD D65–D68, D168); [dailyMinuteOfDay] is minutes after midnight, [todayEpochDay] the phone's
 * date when read, [nextVisitEpochDay] the next doctor visit, if any, and [visitRemovable] whether "Remove" is offered.
 */
@Parcelize
data class RemindersUiModel(
    val dailyEnabled: Boolean,
    val dailyMinuteOfDay: Int,
    val effectEnabled: Boolean,
    val notificationsAllowed: Boolean,
    val mayAskPermission: Boolean,
    val todayEpochDay: Long,
    val nextVisitEpochDay: Long? = null,
    val visitRemovable: Boolean = false,
    val visitChannelOff: Boolean = false
) : Parcelable
