package com.migrely.app.message

import com.migrely.app.datasource.attack.datasource.DELETED_ATTACK_RETENTION_MILLIS
import com.migrely.app.designsystem.snackbar.UNDO_CEILING_MILLIS
import org.junit.Assert.assertEquals
import org.junit.Test

// The Undo leaves this long before its data is purged (BRD D182 (3)).
private const val MARGIN_MILLIS = 5_000L

/**
 * The Undo ceiling lives in `:designsystem` and the deleted attack's retention in `:datasource`; only `:app` sees both.
 * If D23 changes, this fails until the ceiling follows (EC-ATK-24).
 */
class UndoCeilingTest {
    @Test
    fun `The Undo ceiling is the deleted attack's retention less 5 seconds`() {
        assertEquals(DELETED_ATTACK_RETENTION_MILLIS - MARGIN_MILLIS, UNDO_CEILING_MILLIS)
    }
}
