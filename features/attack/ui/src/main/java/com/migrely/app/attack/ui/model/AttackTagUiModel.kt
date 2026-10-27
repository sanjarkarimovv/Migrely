package com.migrely.app.attack.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** One choice on the details screen: where it hurts, a symptom or a trigger. */
sealed interface AttackTagUiModel : Parcelable {
    @Parcelize
    data class Location(val location: HeadLocationUiModel) : AttackTagUiModel

    @Parcelize
    data class Symptom(val symptom: SymptomUiModel) : AttackTagUiModel

    @Parcelize
    data class Trigger(val trigger: TriggerUiModel) : AttackTagUiModel
}
