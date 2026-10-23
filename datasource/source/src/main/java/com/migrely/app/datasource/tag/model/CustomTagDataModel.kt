package com.migrely.app.datasource.tag.model

data class CustomTagDataModel(
    val id: String,
    val kind: CustomTagKindDataModel,
    val name: String,
    val archived: Boolean
)
