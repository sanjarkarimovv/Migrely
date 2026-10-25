package com.migrely.app.settings.domain.model

/**
 * The settings Settings shows. [periodTracking] is "Track periods" (BRD DAY-F1, D79); [version] is the installed
 * app's version (D97).
 */
data class SettingsDomainModel(
    val periodTracking: Boolean,
    val theme: ThemeDomainModel,
    val version: String
)
