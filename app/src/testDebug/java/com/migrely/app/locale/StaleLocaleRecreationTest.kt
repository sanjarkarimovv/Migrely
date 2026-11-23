package com.migrely.app.locale

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * T23 8.2 row 9d on 5570: after a process-death restore, a per-app change back to the language the platform held
 * before the death reached the app with no configuration change, so the activity kept the old language. On start the
 * activity resolves again and is recreated when the language is no longer the one it was built with: once per
 * language, across instances, so a mismatch that persists cannot loop.
 */
class StaleLocaleRecreationTest {
    private var current: Locale = Locale.GERMANY
    private var recreations = 0
    private var channelRenames = 0

    private fun instance(builtWith: Locale = Locale.US, savedRecreatedFor: String? = null) =
        StaleLocaleRecreation(
            builtWith = builtWith,
            current = { current },
            recreate = { recreations++ },
            languageChanged = { channelRenames++ }
        ).apply { restore(savedRecreatedFor) }

    private val classUnderTest = instance()

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

    @Test
    fun `Given a stale activity recreated once when it starts again then not twice`() {
        classUnderTest.onStart(finishing = false, changingConfigurations = false)

        val recreated = classUnderTest.onStart(finishing = false, changingConfigurations = false)

        assertFalse(recreated)
        assertEquals(1, recreations)
    }

    /** cr-de on 9902f069: with no LocaleManager the two reads can differ in every new instance. */
    @Test
    fun `Given a mismatch that persists then two instances in a row recreate once`() {
        val first = instance()
        first.onStart(finishing = false, changingConfigurations = false)
        val second = instance(savedRecreatedFor = first.saved())

        val recreated = second.onStart(finishing = false, changingConfigurations = false)

        assertFalse(recreated)
        assertEquals(1, recreations)
    }

    @Test
    fun `Given a recreate for German then a later change to another language recreates again`() {
        val first = instance()
        first.onStart(finishing = false, changingConfigurations = false)
        current = Locale.UK
        val second = instance(builtWith = Locale.GERMANY, savedRecreatedFor = first.saved())

        val recreated = second.onStart(finishing = false, changingConfigurations = false)

        assertTrue(recreated)
        assertEquals(2, recreations)
    }

    /** The saved language keeps a first-day keyword, so a German with Sunday first is the same language again. */
    @Test
    fun `Given a locale with a first day then its saved form restores the same locale`() {
        current = Locale.forLanguageTag("de-DE-u-fw-sun")
        val first = instance()
        first.onStart(finishing = false, changingConfigurations = false)
        val second = instance(savedRecreatedFor = first.saved())

        assertFalse(second.onStart(finishing = false, changingConfigurations = false))
    }

    @Test
    fun `Given a stale activity when it is recreated then the channel names follow`() {
        classUnderTest.onStart(finishing = false, changingConfigurations = false)

        assertEquals(1, channelRenames)
    }

    @Test
    fun `Given a matching activity when it starts then the channel names are left alone`() {
        current = Locale.US

        classUnderTest.onStart(finishing = false, changingConfigurations = false)

        assertEquals(0, channelRenames)
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
