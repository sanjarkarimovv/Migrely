package com.migrely.app.attack.domain.model

data class AddIntensityPointRequestDomainModel(
    val attackId: String,
    val point: IntensityPointDomainModel
)
