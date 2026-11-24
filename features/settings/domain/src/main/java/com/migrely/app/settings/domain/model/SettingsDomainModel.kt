package com.migrely.app.settings.domain.model

/**
 * The settings Settings shows. [periodTracking] is "Track periods" (BRD DAY-F1, D79); [version] is the installed
 * app's version (D97). [attackInProgress] is null when it cannot be told; "Share and rate" shows only when it is
 * false (D185, EC-SHR-03, EC-SHR-12).
 */
data class SettingsDomainModel(
    val periodTracking: Boolean,
    val theme: ThemeDomainModel,
    val version: String,
    val attackInProgress: Boolean?
)
