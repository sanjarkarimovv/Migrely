package com.migrely.app.ui.theme.mapper

import com.migrely.app.settings.domain.model.AppearanceDomainModel
import com.migrely.app.ui.theme.model.AppearanceUiModel

class AppearanceUiMapper(private val appThemeUiMapper: AppThemeUiMapper) {
    fun toUi(appearance: AppearanceDomainModel) = AppearanceUiModel(
        theme = appThemeUiMapper.toUi(appearance.theme),
        attackInProgress = appearance.attackInProgress
    )
}
