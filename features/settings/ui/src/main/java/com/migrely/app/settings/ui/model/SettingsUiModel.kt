package com.migrely.app.settings.ui.model

/** [deleting] while all data is being deleted. */
data class SettingsUiModel(
    val periodTracking: Boolean,
    val theme: ThemeUiModel,
    val version: String,
    val deleting: Boolean
)
