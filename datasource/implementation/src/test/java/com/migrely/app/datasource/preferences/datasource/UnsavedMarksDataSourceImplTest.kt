package com.migrely.app.datasource.preferences.datasource

import com.migrely.app.datasource.preferences.model.PaywallTriggerDataModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UnsavedMarksDataSourceImplTest {
    private val classUnderTest = UnsavedMarksDataSourceImpl()

    @Test
    fun `Given nothing kept then no marks`() {
        assertEquals(emptySet<PaywallTriggerDataModel>(), classUnderTest.paywallShown())
        assertNull(classUnderTest.lastPaywallShownMillis())
        assertEquals(emptyList<Long>(), classUnderTest.reviewRequestsMillis())
    }

    @Test
    fun `When paywall marks are kept then every trigger and the latest time, in any order`() {
        classUnderTest.keepPaywallShown(PaywallTriggerDataModel.FifthMigraine, 20L)
        classUnderTest.keepPaywallShown(PaywallTriggerDataModel.Onboarding, 10L)

        assertEquals(
            setOf(PaywallTriggerDataModel.FifthMigraine, PaywallTriggerDataModel.Onboarding),
            classUnderTest.paywallShown()
        )
        assertEquals(20L, classUnderTest.lastPaywallShownMillis())
    }

    @Test
    fun `When rating requests are kept then each time, in order`() {
        classUnderTest.keepReviewRequested(5L)
        classUnderTest.keepReviewRequested(9L)

        assertEquals(listOf(5L, 9L), classUnderTest.reviewRequestsMillis())
    }

    @Test
    fun `When safety-card marks are kept then the latest, in any order`() {
        assertNull(classUnderTest.lastSafetyCardShownMillis())

        classUnderTest.keepSafetyCardShown(30L)
        classUnderTest.keepSafetyCardShown(10L)

        assertEquals(30L, classUnderTest.lastSafetyCardShownMillis())
    }
}
