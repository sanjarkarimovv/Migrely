package com.migrely.app.settings.ui.mapper

import com.migrely.app.settings.presentation.model.SettingsPresentationModel
import com.migrely.app.settings.presentation.model.SettingsPresentationNotification
import com.migrely.app.settings.presentation.model.SettingsViewState
import com.migrely.app.settings.ui.model.SettingsUiModel
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SettingsUiMappersTest {
    @Test
    fun `When mapping the state then nothing while loading, then the settings`() {
        assertNull(SettingsViewStateUiMapper().toUi(SettingsViewState.Loading))
        assertEquals(
            SettingsUiModel(periodTracking = false),
            SettingsViewStateUiMapper().toUi(
                SettingsViewState.Ready(SettingsPresentationModel(false))
            )
        )
    }

    @Test
    fun `When a setting was not saved then the presenter says so`() = runTest {
        val presenter = SettingsEventPresenter()
        val shown = async(start = UNDISPATCHED) { presenter.notSaved.first() }

        SettingsNotificationUiMapper(
            presenter
        ).toUi(SettingsPresentationNotification.NotSaved).present()

        assertEquals(Unit, shown.await())
    }
}
