package com.migrely.app.designsystem.input

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Test

private const val THUMBS_UP = "👍"
private const val FAMILY = "👨‍👩‍👧"

class LimitedTextTest {
    @Test
    fun `When an edit fits then it is kept as typed`() {
        assertEquals("abcd", limitedEdit(old = "abc", new = "abcd", maxLength = 5))
    }

    @Test
    fun `Given a full text when typing in the middle then nothing changes`() {
        // QA #80: the last character used to go.
        val full = "a".repeat(5)

        assertEquals(full, limitedEdit(old = full, new = "aaQaaa", maxLength = 5))
    }

    @Test
    fun `Given one unit left when typing an emoji then it is refused whole`() {
        // QA #81: half of the emoji used to be stored.
        val old = "a".repeat(4)

        assertEquals(old, limitedEdit(old = old, new = old + THUMBS_UP, maxLength = 5))
    }

    @Test
    fun `Given room for part of a paste then only whole characters that fit are inserted`() {
        val old = "ab|cd"
        val pasted = "xy$THUMBS_UP$THUMBS_UP"

        // 5 + 6 = 11 units; 3 fit before the first emoji is split: "xy" and nothing of the emoji.
        val actual = limitedEdit(old = old, new = "ab|" + pasted + "cd", maxLength = 8)

        assertEquals("ab|xycd", actual)
    }

    @Test
    fun `Given room for a whole emoji sequence then it is inserted, otherwise none of it`() {
        val old = "a".repeat(2)

        assertEquals(old + FAMILY, limitedEdit(old = old, new = old + FAMILY, maxLength = 10))
        assertEquals(old, limitedEdit(old = old, new = old + FAMILY, maxLength = 9))
    }

    @Test
    fun `Given a selection when a replacement does not fit then only what fits replaces it`() {
        // "bc" selected in "abcd" and replaced by "WXYZ", with room for 4 units in all.
        assertEquals("aWXd", limitedEdit(old = "abcd", new = "aWXYZd", maxLength = 4))
    }

    @Test
    fun `When deleting from a text over the limit then the deletion is kept`() {
        assertEquals("abc", limitedEdit(old = "abcd", new = "abc", maxLength = 2))
    }

    @Test
    fun `Given a full field when a key is refused then the cursor stays where it was`() {
        val old = TextFieldValue("aaaaa", TextRange(2))
        val typed = TextFieldValue("aaQaaa", TextRange(3))

        assertEquals(old, limitedEdit(old, typed, maxLength = 5))
    }

    @Test
    fun `Given a shortened paste then the cursor is right after what was inserted`() {
        val old = TextFieldValue("ab|cd", TextRange(3))
        val pasted = TextFieldValue("ab|xy${THUMBS_UP}cd", TextRange(7))

        assertEquals(
            TextFieldValue("ab|xycd", TextRange(5)),
            limitedEdit(old, pasted, maxLength = 8)
        )
    }
}
