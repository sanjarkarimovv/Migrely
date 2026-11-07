package com.migrely.app.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Test

private const val HELPER_FILE = "MigrelyDialogs"

private val RAW_DIALOGS = setOf(
    "androidx.compose.ui.window.Dialog",
    "androidx.compose.material3.AlertDialog",
    "androidx.compose.material3.BasicAlertDialog",
    "androidx.compose.material3.DatePickerDialog",
    "androidx.compose.material3.TimePickerDialog",
    "androidx.compose.material3.ModalBottomSheet"
)

/**
 * A new dialog that calls Material or Compose directly would answer the second tap of the double
 * tap that opened it (issue #157, BRD D160). Only the helper's file reaches them.
 */
class ArchitectureDialogsTest {
    @Test
    fun `only the helper's file imports a raw dialog`() {
        Konsist.scopeFromProduction()
            .files
            .filter { it.name != HELPER_FILE }
            .assertFalse { file -> file.imports.any { it.name in RAW_DIALOGS } }
    }
}
