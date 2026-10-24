package com.migrely.app.attack.domain.model

data class SetIntakeEffectRequestDomainModel(
    val intakeId: String,
    val effect: AttackIntakeEffectDomainModel
)
