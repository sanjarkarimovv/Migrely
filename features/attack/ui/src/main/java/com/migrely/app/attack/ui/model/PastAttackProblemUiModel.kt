package com.migrely.app.attack.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** Why the add screen did not store the attack; parcelable so it survives rotation. */
sealed interface PastAttackProblemUiModel : Parcelable {
    /** An inline error under one of the times (BRD EC-ATK-03, EC-ATK-04). */
    @Parcelize
    data class Time(val error: TimeErrorUiModel) : PastAttackProblemUiModel

    /** The overlap card naming [attacks], oldest first (BRD EC-ATK-05, EC-ATK-15). */
    @Parcelize
    data class Overlaps(val attacks: List<AttackRangeUiModel>) : PastAttackProblemUiModel
}
