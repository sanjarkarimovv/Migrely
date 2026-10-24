package com.migrely.app.attack.domain.model

data class RestoreIntakeRequestDomainModel(
    val attackId: String,
    val intake: AttackIntakeDomainModel
)
