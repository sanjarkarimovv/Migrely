package com.migrely.app.home.ui.view

import org.junit.Assert.assertEquals
import org.junit.Test

internal class MessagesClearSettingsRowTest {
    @Test
    fun `On Today in a tall window a message moves below the Settings row`() {
        assertEquals(true, messagesClearSettingsRow(HomeMode.Today, compactHeight = false))
    }

    @Test
    fun `On Today in a short window a message stays clear of Migraine started`() {
        assertEquals(false, messagesClearSettingsRow(HomeMode.Today, compactHeight = true))
    }

    @Test
    fun `In attack mode a message keeps its own place in any window`() {
        assertEquals(false, messagesClearSettingsRow(HomeMode.Attack, compactHeight = false))
        assertEquals(false, messagesClearSettingsRow(HomeMode.Attack, compactHeight = true))
    }
}
