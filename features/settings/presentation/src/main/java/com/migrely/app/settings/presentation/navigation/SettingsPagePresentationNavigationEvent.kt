package com.migrely.app.settings.presentation.navigation

import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent
import com.migrely.app.settings.presentation.model.SettingsPagePresentationModel

sealed interface SettingsPagePresentationNavigationEvent : PresentationNavigationEvent {
    /** Leaves [page]. */
    data class OnBack(val page: SettingsPagePresentationModel) :
        SettingsPagePresentationNavigationEvent
}
