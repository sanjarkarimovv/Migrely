package com.migrely.app.settings.presentation.mapper

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class DeleteConfirmationResolverTest(private val typed: String, private val confirms: Boolean) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "\"{0}\" confirms: {1}")
        fun parameters() = listOf(
            arrayOf<Any?>("DELETE", true),
            arrayOf<Any?>("delete", false),
            arrayOf<Any?>("Delete", false),
            arrayOf<Any?>("DELETE ", false),
            arrayOf<Any?>(" DELETE", false),
            arrayOf<Any?>("DELET", false),
            arrayOf<Any?>("", false)
        )
    }

    @Test
    fun `When typed then only the exact word confirms`() {
        assertEquals(confirms, DeleteConfirmationResolver().resolve(typed))
    }
}
