package com.migrely.app.attack.ui.view

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

/**
 * One spoken name per control (BRD D181 (2), EC-UX-45): the control's merged node has [spoken] as
 * its only description, keeps its click, exposes no text of its own, and [spoken] contains the
 * [visible] words in order (label in name, WCAG 2.5.3). The same helper sits in each UI module's
 * tests, which share no test module.
 */
internal fun ComposeTestRule.assertOneSpokenName(spoken: String, visible: String) {
    val config = onNodeWithContentDescription(spoken)
        .assertHasClickAction()
        .fetchSemanticsNode()
        .config
    assertEquals(listOf(spoken), config.getOrNull(SemanticsProperties.ContentDescription))
    assertNull("'$spoken' also reads its text", config.getOrNull(SemanticsProperties.Text))
    assertTrue("'$visible' is not in '$spoken'", labelInName(spoken, visible))
}

private fun labelInName(spoken: String, visible: String): Boolean {
    val said = words(spoken)
    var from = 0
    return words(visible).all { word ->
        val at = said.subList(from, said.size).indexOf(word)
        if (at >= 0) from += at + 1
        at >= 0
    }
}

/** Letters and digits only, so "+ Your own" and "14:30" compare by their words. */
private fun words(text: String) =
    Regex("""[\p{L}\p{N}]+""").findAll(text.lowercase()).map { it.value }.toList()
