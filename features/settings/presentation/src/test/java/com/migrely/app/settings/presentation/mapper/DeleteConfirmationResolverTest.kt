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
            arrayOf("DELETE", true),
            arrayOf("delete", false),
            arrayOf("Delete", false),
            arrayOf("DELETE ", false),
            arrayOf(" DELETE", false),
            arrayOf("DELET", false),
            arrayOf("", false)
        )
    }

    @Test
    fun `When typed then only the exact word confirms`() {
        assertEquals(confirms, DeleteConfirmationResolver().resolve(typed))
    }
}
