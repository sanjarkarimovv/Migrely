package com.migrely.app.attack.domain.model

data class AddCustomTagRequestDomainModel(
    val attackId: String,
    val kind: TagKindDomainModel,
    val name: String
)
