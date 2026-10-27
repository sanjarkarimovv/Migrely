package com.migrely.app.settings.presentation.model

data class SettingsPresentationModel(
    val periodTracking: Boolean,
    val theme: ThemePresentationModel,
    val version: String
)
