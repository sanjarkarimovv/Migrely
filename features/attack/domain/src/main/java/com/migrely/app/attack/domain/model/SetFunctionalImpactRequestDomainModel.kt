package com.migrely.app.attack.domain.model

/** A null [impact] clears it. */
data class SetFunctionalImpactRequestDomainModel(
    val attackId: String,
    val impact: FunctionalImpactDomainModel?
)
