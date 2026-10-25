package com.migrely.app.settings.domain.model

/**
 * How the app looks (BRD D94): the [theme] the user chose, and whether an attack is in progress, during which every
 * screen is dark whatever the theme, because light hurts during an attack.
 */
data class AppearanceDomainModel(val theme: ThemeDomainModel, val attackInProgress: Boolean)
