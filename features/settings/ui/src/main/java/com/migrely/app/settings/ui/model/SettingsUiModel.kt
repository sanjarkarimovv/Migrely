package com.migrely.app.settings.ui.model

/** [showShareAndRate] is false during an attack and while that cannot be told (BRD D185, EC-SHR-03, EC-SHR-12). */
data class SettingsUiModel(
    val periodTracking: Boolean,
    val theme: ThemeUiModel,
    val version: String,
    val showShareAndRate: Boolean
)
