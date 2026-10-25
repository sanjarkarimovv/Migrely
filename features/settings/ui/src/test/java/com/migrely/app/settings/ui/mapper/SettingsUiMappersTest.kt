package com.migrely.app.settings.ui.mapper

import com.migrely.app.settings.presentation.model.SettingsPresentationModel
import com.migrely.app.settings.presentation.model.SettingsPresentationNotification
import com.migrely.app.settings.presentation.model.SettingsViewState
import com.migrely.app.settings.presentation.model.ThemePresentationModel
import com.migrely.app.settings.ui.model.SettingsUiModel
import com.migrely.app.settings.ui.model.ThemeUiModel
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

class SettingsUiMappersTest {
    @Test
    fun `When mapping the state then nothing while loading, then the settings`() {
        val mapper = SettingsViewStateUiMapper(ThemeUiMapper())

        assertNull(mapper.toUi(SettingsViewState.Loading))
        assertEquals(
            SettingsUiModel(periodTracking = false, theme = ThemeUiModel.Light, version = "1.2.0"),
            mapper.toUi(
                SettingsViewState.Ready(
                    SettingsPresentationModel(false, ThemePresentationModel.Light, "1.2.0")
                )
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

@RunWith(Parameterized::class)
class ThemeUiMappersTest(
    private val presentation: ThemePresentationModel,
    private val ui: ThemeUiModel
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} is {1}")
        fun parameters() = listOf(
            arrayOf(ThemePresentationModel.Dark, ThemeUiModel.Dark),
            arrayOf(ThemePresentationModel.Light, ThemeUiModel.Light),
            arrayOf(ThemePresentationModel.System, ThemeUiModel.System)
        )
    }

    @Test
    fun `When mapping either way then the same theme`() {
        assertEquals(ui, ThemeUiMapper().toUi(presentation))
        assertEquals(presentation, ThemeUiToPresentationMapper().toPresentation(ui))
    }
}
