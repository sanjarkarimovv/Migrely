package com.migrely.app.ui.theme.model

/** How the app is drawn (BRD D94): the chosen [theme], and whether an attack is in progress, which makes it dark. */
data class AppearanceUiModel(val theme: AppThemeUiModel, val attackInProgress: Boolean)
