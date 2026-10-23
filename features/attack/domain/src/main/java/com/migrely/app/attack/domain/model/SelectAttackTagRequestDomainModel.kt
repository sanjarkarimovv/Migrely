package com.migrely.app.attack.domain.model

data class SelectAttackTagRequestDomainModel(
    val attackId: String,
    val tag: AttackTagDomainModel,
    val selected: Boolean
)
