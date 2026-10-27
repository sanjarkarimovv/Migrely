package com.migrely.app.attack.presentation.model

data class CustomTagPresentationModel(
    val id: String,
    val kind: TagKindPresentationModel,
    val name: String,
    val archived: Boolean
)
