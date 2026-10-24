package com.migrely.app.settings.domain.model

/** The app's look (BRD SET-F1, D94). Attack mode stays dark in each. */
enum class ThemeDomainModel {
    Dark,
    Light,

    /** Follows the phone's dark theme setting. */
    System
}
