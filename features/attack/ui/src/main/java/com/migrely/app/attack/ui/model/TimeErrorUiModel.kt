package com.migrely.app.attack.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** An inline error under the start or end time (BRD EC-ATK-03, EC-ATK-04, EC-ATK-05). */
@Parcelize
data class TimeErrorUiModel(val field: TimeFieldUiModel, val reason: TimeErrorReasonUiModel) :
    Parcelable

sealed interface TimeErrorReasonUiModel : Parcelable {
    @Parcelize
    data object InFuture : TimeErrorReasonUiModel

    @Parcelize
    data object EndBeforeStart : TimeErrorReasonUiModel

    /**
     * The times overlap [attacks], oldest first (BRD EC-ATK-15). The refused times go back with "Merge them".
     */
    @Parcelize
    data class Overlaps(
        val attacks: List<AttackRangeUiModel>,
        val requestedStartEpochMillis: Long,
        val requestedEndEpochMillis: Long?
    ) : TimeErrorReasonUiModel
}
