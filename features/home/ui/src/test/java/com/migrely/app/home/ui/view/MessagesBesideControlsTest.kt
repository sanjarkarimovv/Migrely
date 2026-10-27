package com.migrely.app.home.ui.view

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
internal class MessagesBesideControlsTest(
    private val mode: HomeMode,
    private val compactHeight: Boolean,
    private val expected: Boolean
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}, compact height {1}: beside {2}")
        fun data(): Collection<Array<Any>> = listOf(
            // Landscape attack mode: the controls fill the right column from the top (QA #20).
            arrayOf(HomeMode.Attack, true, true),
            // Portrait attack mode keeps its controls in the lower half.
            arrayOf(HomeMode.Attack, false, false),
            arrayOf(HomeMode.Today, true, false),
            arrayOf(HomeMode.Today, false, false),
            arrayOf(HomeMode.Loading, true, false),
            arrayOf(HomeMode.Loading, false, false)
        )
    }

    @Test
    fun `When a message shows then it keeps beside the controls only in landscape attack mode`() {
        assertEquals(expected, messagesBesideControls(mode, compactHeight))
    }
}
