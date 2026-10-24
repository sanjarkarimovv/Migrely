package com.migrely.app.home.ui.view

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
internal class GuardsTapsTest(
    private val previous: HomeMode,
    private val next: HomeMode,
    private val expected: Boolean
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} -> {1}: guard {2}")
        fun data(): Collection<Array<Any>> = listOf(
            // A double tap on "Migraine started" must not reach "Migraine ended", and the reverse (QA #12).
            arrayOf(HomeMode.Today, HomeMode.Attack, true),
            arrayOf(HomeMode.Attack, HomeMode.Today, true),
            // Opening the app or rotating starts from Loading: no wait.
            arrayOf(HomeMode.Loading, HomeMode.Today, false),
            arrayOf(HomeMode.Loading, HomeMode.Attack, false),
            arrayOf(HomeMode.Today, HomeMode.Today, false),
            arrayOf(HomeMode.Attack, HomeMode.Attack, false),
            // Never reached in practice (home does not go back to Loading); guarding is harmless.
            arrayOf(HomeMode.Today, HomeMode.Loading, true)
        )
    }

    @Test
    fun `When the mode is shown then guards taps only after the other mode`() {
        assertEquals(expected, guardsTaps(previous, next))
    }
}
