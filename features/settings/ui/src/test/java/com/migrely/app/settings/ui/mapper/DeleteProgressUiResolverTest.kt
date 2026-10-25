package com.migrely.app.settings.ui.mapper

import com.migrely.app.settings.ui.model.SettingsUiModel
import com.migrely.app.settings.ui.model.ThemeUiModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

private fun settings(deleting: Boolean) = SettingsUiModel(
    periodTracking = true,
    theme = ThemeUiModel.Dark,
    version = "1.2.0",
    deleting = deleting
)

@RunWith(Parameterized::class)
class DeleteProgressUiResolverTest(
    private val case: String,
    private val started: Boolean,
    private val settings: SettingsUiModel?,
    private val shown: Boolean
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun parameters() = listOf(
            arrayOf("not started", false, settings(deleting = false), false),
            arrayOf("confirmed, before the state arrives", true, null, true),
            arrayOf("confirmed, before the state says deleting", true, settings(false), true),
            arrayOf("rebuilt while deleting", false, settings(deleting = true), true),
            arrayOf("rebuilt with the screen's flag kept", true, null, true),
            arrayOf("deleted, until the restart", true, settings(deleting = true), true)
        )
    }

    @Test
    fun `When deciding then the progress shows from the confirmation on`() {
        assertEquals(shown, DeleteProgressUiResolver().resolve(started, settings))
    }
}
