package com.migrely.app.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Test

/** By its place in the project, so a file of the same name elsewhere is checked like any other. */
private const val HELPER_PATH =
    "/designsystem/src/main/java/com/migrely/app/designsystem/dialog/MigrelyMenus.kt"

/**
 * Read in the file's text, which holds an import and a fully qualified call alike. The `\b` lets
 * `DropdownMenuItem`, `PopupProperties` and `PopupPositionProvider` through: none is a window.
 * Each name here opens a popup window of its own: Compose's directly, or a View through a
 * `ListPopupWindow` (the spinners and the auto-complete fields). On `app`'s classpath through
 * Material Components and AppCompat.
 */
private val RAW_POPUPS = listOf(
    "androidx.compose.material3.DropdownMenu",
    "androidx.compose.material3.ExposedDropdownMenuBox",
    "androidx.compose.material3.TooltipBox",
    "androidx.compose.material3.ExpandedDockedSearchBar",
    "androidx.compose.foundation.BasicTooltipBox",
    "androidx.compose.ui.window.Popup",
    "android.widget.PopupMenu",
    "android.widget.PopupWindow",
    "android.widget.ListPopupWindow",
    "android.widget.Spinner",
    "android.widget.AutoCompleteTextView",
    "android.widget.MultiAutoCompleteTextView",
    "androidx.appcompat.widget.PopupMenu",
    "androidx.appcompat.widget.ListPopupWindow",
    "androidx.appcompat.widget.AppCompatSpinner",
    "androidx.appcompat.widget.AppCompatAutoCompleteTextView",
    "androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView",
    "com.google.android.material.textfield.MaterialAutoCompleteTextView"
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
            .filter { it.projectPath != HELPER_PATH }
            .assertFalse { file -> RAW_POPUPS.any { it.containsMatchIn(file.text) } }
    }
}
