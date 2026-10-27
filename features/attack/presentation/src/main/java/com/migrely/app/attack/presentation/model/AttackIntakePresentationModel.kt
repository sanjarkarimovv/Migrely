package com.migrely.app.attack.presentation.model

import java.time.Instant
import java.time.ZoneId

data class AttackIntakePresentationModel(
    val id: String,
    val medicationId: String,
    val medicationName: String,
    val dose: String?,
    val takenAt: Instant,
    val zone: ZoneId,
    val effect: AttackIntakeEffectPresentationModel? = null,
    val effectQuestionClosed: Boolean = false
)
