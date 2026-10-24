package com.migrely.app.attack.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** A medication taken during the attack (BRD ATK-F5, D34). */
@Parcelize
data class AttackIntakeUiModel(
    val id: String,
    val medicationId: String,
    val medicationName: String,
    val dose: String?,
    val takenAtEpochMillis: Long,
    val zoneId: String,
    val effect: AttackIntakeEffectUiModel? = null,
    val effectQuestionClosed: Boolean = false
) : Parcelable
