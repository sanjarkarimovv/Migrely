package com.migrely.app.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Test

/** By its place in the project, so a file of the same name elsewhere is checked like any other. */
private const val HELPER_PATH =
    "/designsystem/src/main/java/com/migrely/app/designsystem/dialog/MigrelyDialogs.kt"

/** Read in the file's text, which holds an import and a fully qualified call alike. */
private val RAW_DIALOGS = listOf(
    "androidx.compose.ui.window.Dialog",
    "androidx.compose.material3.AlertDialog",
    "androidx.compose.material3.BasicAlertDialog",
    "androidx.compose.material3.DatePickerDialog",
    "androidx.compose.material3.TimePickerDialog",
    "androidx.compose.material3.ModalBottomSheet",
    "androidx.compose.material3.ExpandedFullScreenSearchBar",
    "androidx.compose.material3.ModalWideNavigationRail"
).map { name -> Regex(Regex.escape(name) + "\\b") }

/**
 * A new dialog that calls Material or Compose directly would answer the second tap of the double
 * tap that opened it (issue #157, BRD D160). Only the helper's file names them. A wildcard import
 * would get past this, and ktlint refuses one.
 */
class ArchitectureDialogsTest {
    @Test
    fun `only the helper's file names a raw dialog`() {
        Konsist.scopeFromProduction()
            .files
            .filter { it.projectPath != HELPER_PATH }
            .assertFalse { file -> RAW_DIALOGS.any { it.containsMatchIn(file.text) } }
    }
}
