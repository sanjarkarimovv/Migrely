package com.migrely.app.ui.theme.mapper

import com.migrely.app.ui.theme.model.AppThemeUiModel
import com.migrely.app.ui.theme.model.AppearanceUiModel

/**
 * Whether every screen is drawn dark (BRD D94): while an attack is in progress, whatever the theme, because light
 * hurts during an attack; otherwise as chosen, "Match the phone" following [systemDark].
 */
class DarkThemeUiResolver {
    fun resolve(appearance: AppearanceUiModel, systemDark: Boolean): Boolean =
        appearance.attackInProgress || when (appearance.theme) {
            AppThemeUiModel.Dark -> true
            AppThemeUiModel.Light -> false
            AppThemeUiModel.System -> systemDark
        }
}
