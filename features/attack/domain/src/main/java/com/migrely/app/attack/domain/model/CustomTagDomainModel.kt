package com.migrely.app.attack.domain.model

/** A symptom or trigger the user added. An [archived] tag is no longer offered but stays on attacks that use it. */
data class CustomTagDomainModel(
    val id: String,
    val kind: TagKindDomainModel,
    val name: String,
    val archived: Boolean
)
