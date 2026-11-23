package com.migrely.app.locale

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * T23 8.2 row 9d on 5570: after a process-death restore, a per-app change back to the language the platform held
 * before the death reached the app with no configuration change, so the activity kept the old language. On start the
 * activity resolves again and is recreated, once, when the language is no longer the one it was built with.
 */
class StaleLocaleRecreationTest {
    private var current: Locale = Locale.GERMANY
    private var recreations = 0

    private val classUnderTest = StaleLocaleRecreation(
        builtWith = Locale.US,
        current = { current },
        recreate = { recreations++ }
    )

    @Test
    fun `Given a stale activity when it starts then it is recreated`() {
        val recreated = classUnderTest.onStart(finishing = false, changingConfigurations = false)

        assertTrue(recreated)
        assertEquals(1, recreations)
    }

    @Test
    fun `Given a matching activity when it starts then it is not recreated`() {
        current = Locale.US

        val recreated = classUnderTest.onStart(finishing = false, changingConfigurations = false)

        assertFalse(recreated)
        assertEquals(0, recreations)
    }

    /** Even if the platform kept handing back a stale configuration, the activity cannot loop. */
    @Test
    fun `Given a stale activity recreated once when it starts again then not twice`() {
        classUnderTest.onStart(finishing = false, changingConfigurations = false)

        val recreated = classUnderTest.onStart(finishing = false, changingConfigurations = false)

        assertFalse(recreated)
        assertEquals(1, recreations)
    }

    @Test
    fun `Given a finishing activity when it starts then it is not recreated`() {
        classUnderTest.onStart(finishing = true, changingConfigurations = false)

        assertEquals(0, recreations)
    }

    @Test
    fun `Given an activity changing configuration when it starts then it is not recreated`() {
        classUnderTest.onStart(finishing = false, changingConfigurations = true)

        assertEquals(0, recreations)
    }
}
