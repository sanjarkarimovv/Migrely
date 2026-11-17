package com.migrely.app.attack.ui.view

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.withKeyDown

/**
 * Hardware-keyboard walks for the order tests (BRD D181 (6)): real key events on the root, so the
 * input mode is Keyboard as on a device, and each stop named by what TalkBack would read first.
 * The same helper sits in each UI module's tests, which share no test module.
 */
@OptIn(ExperimentalTestApi::class)
internal fun ComposeTestRule.press(key: Key, shift: Boolean = false) {
    onRoot().performKeyInput {
        if (shift) withKeyDown(Key.ShiftLeft) { pressKey(key) } else pressKey(key)
    }
    waitForIdle()
}

/** The focused node's description, or else its first text; null when nothing has focus. */
internal fun ComposeTestRule.focusedName(): String? {
    val node = onAllNodes(isFocused()).fetchSemanticsNodes().singleOrNull() ?: return null
    return node.config.getOrNull(SemanticsProperties.ContentDescription)?.firstOrNull()
        ?: node.config.getOrNull(SemanticsProperties.Text)?.firstOrNull()?.text
}

/** The stops reached by [count] presses of Tab, or Shift+Tab when [back]. */
internal fun ComposeTestRule.walk(count: Int, back: Boolean = false): List<String?> =
    (1..count).map {
        press(Key.Tab, shift = back)
        focusedName()
    }
