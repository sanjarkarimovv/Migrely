package com.migrely.app.medication.domain.model

data class AcknowledgeOveruseRequestDomainModel(
    val category: OveruseCategoryDomainModel,
    val level: OveruseLevelDomainModel
)
