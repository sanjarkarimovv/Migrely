package com.migrely.app.settings.presentation.model

/** [showShareAndRate] is false during an attack and whenever that cannot be told (BRD D185, EC-SHR-03, EC-SHR-12). */
data class SettingsPresentationModel(
    val periodTracking: Boolean,
    val theme: ThemePresentationModel,
    val version: String,
    val showShareAndRate: Boolean
)
