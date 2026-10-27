package com.migrely.app.datasource.medication.model

import java.time.Instant

/**
 * A medication taken; [medicationName] is the medication's current name, archived or not. [effect] is the answer to
 * "Did it help?", and [effectClosed] is true once it was answered or skipped (BRD D39).
 */
data class IntakeDataModel(
    val id: String,
    val medicationId: String,
    val medicationName: String,
    val attackId: String?,
    val takenAt: Instant,
    val takenZoneId: String,
    val dose: String?,
    val effect: IntakeEffectDataModel? = null,
    val effectClosed: Boolean = false
)
