package com.migrely.app.designsystem.dialog

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** A wide alert dialog may grow past the platform's default width (T23 8.1 row R03); others keep it. */
class AlertDialogPropertiesTest {
    @Test
    fun `Given a wide dialog then it leaves the platform's default width`() {
        assertFalse(alertDialogProperties(wide = true).usePlatformDefaultWidth)
    }

    @Test
    fun `Given a normal dialog then it keeps the platform's default width`() {
        assertTrue(alertDialogProperties(wide = false).usePlatformDefaultWidth)
    }
}
