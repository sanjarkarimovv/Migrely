package com.migrely.app.attack.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

private const val MILLIS_PER_MINUTE = 60_000L

/** The times picked on the add screen, shown in [zoneId]; saved across rotation and process death (BRD EC-ATK-10). */
@Parcelize
data class PastAttackDraftUiModel(
    val startEpochMillis: Long,
    val endEpochMillis: Long,
    val zoneId: String
) : Parcelable {
    /**
     * A new start carries the end along, keeping the duration as in a calendar, but the end never passes
     * [latestEndEpochMillis] and stays at least a minute after the start (BRD D26).
     */
    fun withStart(startEpochMillis: Long, latestEndEpochMillis: Long): PastAttackDraftUiModel {
        val duration = endEpochMillis - this.startEpochMillis
        val end = minOf(startEpochMillis + duration, latestEndEpochMillis)
            .coerceAtLeast(startEpochMillis + MILLIS_PER_MINUTE)
        return copy(startEpochMillis = startEpochMillis, endEpochMillis = end)
    }
}
