package com.migrely.app.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Test

private const val HELPER_FILE = "MigrelyMenus"

/**
 * Read in the file's text, which holds an import and a fully qualified call alike. The `\b` lets
 * `DropdownMenuItem`, `PopupProperties` and `PopupPositionProvider` through: none is a window.
 */
private val RAW_POPUPS = listOf(
    "androidx.compose.material3.DropdownMenu",
    "androidx.compose.material3.ExposedDropdownMenuBox",
    "androidx.compose.material3.TooltipBox",
    "androidx.compose.ui.window.Popup",
    "android.widget.PopupMenu",
    "android.widget.PopupWindow",
    "android.widget.ListPopupWindow"
).map { name -> Regex(Regex.escape(name) + "\\b") }

/**
 * A popup is a window of its own. The first tap of a double tap can close it, and the second then
 * lands on the screen beneath and writes there (issue #161, BRD D164). Only the helper's file names
 * a raw popup. When this fails, open that popup through a helper in `designsystem`, package
 * `dialog`, that puts `liftOnCard()` on its card, as `MigrelyDropdownMenu` does. A wildcard import
 * would get past this, and ktlint refuses one.
 */
class ArchitecturePopupsTest {
    @Test
    fun `only the helper's file names a raw popup`() {
        Konsist.scopeFromProduction()
            .files
            .filter { it.name != HELPER_FILE }
            .assertFalse { file -> RAW_POPUPS.any { it.containsMatchIn(file.text) } }
    }
}
