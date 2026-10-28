package com.migrely.app.designsystem.snackbar

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private const val NOW = 1_793_161_943_447L

class DeletionOfferTest {
    @Test
    fun `Given a deletion just made then it is offered`() {
        assertTrue(isFreshDeletion(deletedAtMillis = NOW, nowMillis = NOW))
        assertTrue(isFreshDeletion(deletedAtMillis = NOW - 30_000L, nowMillis = NOW))
    }

    @Test
    fun `Given a deletion more than 30 seconds old then it is not offered`() {
        assertFalse(isFreshDeletion(deletedAtMillis = NOW - 30_001L, nowMillis = NOW))
    }

    @Test
    fun `Given a deletion stamped ahead of the clock then only a moment ahead is offered`() {
        assertTrue(isFreshDeletion(deletedAtMillis = NOW + 2_000L, nowMillis = NOW))
        assertFalse(isFreshDeletion(deletedAtMillis = NOW + 2_001L, nowMillis = NOW))
        assertFalse(isFreshDeletion(deletedAtMillis = NOW + 60_000L, nowMillis = NOW))
        assertFalse(isFreshDeletion(deletedAtMillis = NOW + 3 * 86_400_000L, nowMillis = NOW))
    }
}
