package com.migrely.app.attack.domain.model

data class RemoveIntensityPointRequestDomainModel(
    val attackId: String,
    val point: IntensityPointDomainModel
)
