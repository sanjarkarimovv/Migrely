package com.migrely.app.activity

import android.app.Activity
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class CurrentActivityHolderTest {
    private val classUnderTest = CurrentActivityHolder()
    private val main: Activity = mock()
    private val sheet: Activity = mock()

    @Test
    fun `Given an activity resumes then it is the one on screen until it pauses`() {
        classUnderTest.onActivityResumed(main)
        assertSame(main, classUnderTest.resumed())

        classUnderTest.onActivityPaused(main)
        assertNull(classUnderTest.resumed())
    }

    @Test
    fun `Given another activity pauses or goes then the one on screen stays`() {
        classUnderTest.onActivityResumed(sheet)
        classUnderTest.onActivityResumed(main)

        classUnderTest.onActivityPaused(sheet)
        classUnderTest.onActivityDestroyed(sheet)

        assertSame(main, classUnderTest.resumed())
    }

    @Test
    fun `Given the activity on screen is finishing or destroyed then none is given`() {
        val finishing: Activity = mock { on { isFinishing } doReturn true }
        val destroyed: Activity = mock { on { isDestroyed } doReturn true }

        classUnderTest.onActivityResumed(finishing)
        assertNull(classUnderTest.resumed())

        classUnderTest.onActivityResumed(destroyed)
        assertNull(classUnderTest.resumed())
    }

    @Test
    fun `Given the activity on screen is destroyed then it is let go`() {
        classUnderTest.onActivityResumed(main)

        classUnderTest.onActivityDestroyed(main)

        assertNull(classUnderTest.resumed())
    }
}
